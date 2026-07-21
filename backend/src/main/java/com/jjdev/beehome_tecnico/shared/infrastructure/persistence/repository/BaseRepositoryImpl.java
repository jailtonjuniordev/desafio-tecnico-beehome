package com.jjdev.beehome_tecnico.shared.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class BaseRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> {

    @PersistenceContext
    private EntityManager entityManager;
    private final Class<T> domainClass;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation,
                              EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.domainClass = entityInformation.getJavaType();
        log.info("BaseRepositoryImpl inicializado para a entidade: {}", domainClass.getSimpleName());
    }

    protected boolean isStringField(Class<?> clazz, String fieldName) {
        log.debug("Verificando se o campo '{}' da classe '{}' é do tipo String", fieldName, clazz.getSimpleName());
        try {
            Field field = findField(clazz, fieldName);
            boolean isString = field != null && (field.getType() == String.class);
            log.debug("Campo '{}': {} tipo String", fieldName, isString ? "é" : "não é");
            return isString;
        } catch (Exception e) {
            log.warn("Erro ao verificar tipo do campo '{}': {}", fieldName, e.getMessage());
            return false;
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        log.debug("Buscando campo '{}' na classe '{}'", fieldName, clazz.getSimpleName());
        try {
            Field field = clazz.getDeclaredField(fieldName);
            log.debug("Campo '{}' encontrado na classe '{}'", fieldName, clazz.getSimpleName());
            return field;
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                log.debug("Campo '{}' não encontrado em '{}', buscando na superclasse '{}'",
                        fieldName, clazz.getSimpleName(), superClass.getSimpleName());
                return findField(superClass, fieldName);
            }
            log.debug("Campo '{}' não encontrado na hierarquia de classes", fieldName);
            return null;
        }
    }

    protected List<String> getStringFieldNames(Class<?> clazz) {
        log.info("Obtendo campos do tipo String para a classe '{}'", clazz.getSimpleName());
        List<String> fieldNames = new ArrayList<>();
        collectFieldNames(clazz, fieldNames);

        List<String> stringFields = fieldNames.stream()
                .filter(fieldName -> isStringField(clazz, fieldName))
                .toList();

        log.info("Encontrados {} campos do tipo String em '{}'", stringFields.size(), clazz.getSimpleName());
        log.debug("Campos String encontrados: {}", stringFields);
        return stringFields;
    }

    private void collectFieldNames(Class<?> clazz, List<String> fieldNames) {
        if (clazz == null || clazz == Object.class) {
            log.debug("Limite da hierarquia de classes atingido");
            return;
        }

        log.debug("Coletando campos da classe '{}'", clazz.getSimpleName());
        int initialSize = fieldNames.size();

        for (Field field : clazz.getDeclaredFields()) {
            fieldNames.add(field.getName());
        }

        log.debug("Adicionados {} campos da classe '{}'", fieldNames.size() - initialSize, clazz.getSimpleName());

        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            log.debug("Buscando campos na superclasse '{}'", superClass.getSimpleName());
            collectFieldNames(clazz.getSuperclass(), fieldNames);
        }
    }

    private boolean hasField(Class<?> clazz, String fieldName) {
        log.debug("Verificando se a classe '{}' possui o campo '{}'",
                clazz.getSimpleName(), fieldName);

        if (clazz == Object.class) {
            log.debug("Classe é null ou Object, campo '{}' não encontrado", fieldName);
            return false;
        }

        try {
            clazz.getDeclaredField(fieldName);
            log.debug("Campo '{}' encontrado na classe '{}'", fieldName, clazz.getSimpleName());
            return true;
        } catch (NoSuchFieldException e) {
            log.debug("Campo '{}' não encontrado em '{}', verificando superclasse",
                    fieldName, clazz.getSimpleName());
            return hasField(clazz.getSuperclass(), fieldName);
        }
    }

    private Path<?> resolvePath(Root<T> root, String fieldPath) {
        try {
            log.debug("Resolvendo caminho de campo: '{}'", fieldPath);

            String[] pathParts = fieldPath.split("\\.");
            Path<?> currentPath = root;

            for (int i = 0; i < pathParts.length; i++) {
                String part = pathParts[i];
                log.debug("Processando parte do caminho: '{}' (índice {})", part, i);

                if (i == 0) {
                    if (!hasField(domainClass, part)) {
                        log.warn("Campo '{}' não encontrado na entidade '{}'", part, domainClass.getSimpleName());
                        return null;
                    }
                    currentPath = root.get(part);
                } else {
                    try {
                        currentPath = currentPath.get(part);
                        log.debug("Navegando para campo '{}' através do relacionamento", part);
                    } catch (Exception e) {
                        log.warn("Erro ao navegar para campo '{}': {}", part, e.getMessage());
                        return null;
                    }
                }
            }

            log.debug("Caminho '{}' resolvido com sucesso", fieldPath);
            return currentPath;

        } catch (Exception e) {
            log.warn("Erro ao resolver caminho '{}': {}", fieldPath, e.getMessage());
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Page<T> dynamicSearchText(String searchText, Pageable pageable) {
        try {
            log.info("Iniciando busca dinâmica na entidade '{}'. Termo: '{}', Página: {}, Tamanho: {}",
                    domainClass.getSimpleName(),
                    searchText,
                    pageable.getPageNumber(),
                    pageable.getPageSize());

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();

            Long totalCount = createCountQuery(cb, searchText);
            List<T> results = createResultQuery(cb, searchText, pageable);

            return new PageImpl<>(results, pageable, totalCount);

        } catch (Exception e) {
            log.error("Erro na busca dinâmica: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao realizar busca dinâmica", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<T> dynamicSearchFilters(Map<String, Object> filters, Pageable pageable) {
        try {
            log.info("Iniciando busca dinâmica na entidade '{}'. Filtros: {}, Página: {}, Tamanho: {}",
                    domainClass.getSimpleName(),
                    filters,
                    pageable.getPageNumber(),
                    pageable.getPageSize());

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();

            Long totalCount = createCountQuery(cb, filters);
            List<T> results = createResultQuery(cb, filters, pageable);

            return new PageImpl<>(results, pageable, totalCount);

        } catch (Exception e) {
            log.error("Erro na busca dinâmica: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao realizar busca dinâmica", e);
        }
    }

    private Long createCountQuery(CriteriaBuilder cb, String searchText) {
        try {
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<T> countRoot = countQuery.from(domainClass);

            List<Predicate> predicates = new ArrayList<>();

            if (hasField(domainClass, "deletedAt")) {
                predicates.add(cb.isNull(countRoot.get("deletedAt")));
            }

            if (StringUtils.hasText(searchText)) {
                List<Predicate> searchPredicates = createSearchPredicates(cb, countRoot, searchText);
                if (!searchPredicates.isEmpty()) {
                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                }
            }

            countQuery.select(cb.count(countRoot))
                    .where(cb.and(predicates.toArray(new Predicate[0])));

            return entityManager.createQuery(countQuery).getSingleResult();

        } catch (Exception e) {
            log.error("Erro na consulta de contagem: {}", e.getMessage(), e);
            return 0L;
        }
    }

    private Long createCountQuery(CriteriaBuilder cb, Map<String, Object> filters) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(domainClass);

        List<Predicate> predicates = new ArrayList<>();

        addFilterPredicates(cb, countRoot, predicates, filters);

        countQuery.select(cb.count(countRoot))
                .where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<T> createResultQuery(CriteriaBuilder cb, String searchText, Pageable pageable) {
        try {
            CriteriaQuery<T> query = cb.createQuery(domainClass);
            Root<T> root = query.from(domainClass);

            List<Predicate> predicates = new ArrayList<>();

            if (hasField(domainClass, "deletedAt")) {
                predicates.add(cb.isNull(root.get("deletedAt")));
            }

            if (StringUtils.hasText(searchText)) {
                List<Predicate> searchPredicates = createSearchPredicates(cb, root, searchText);
                if (!searchPredicates.isEmpty()) {
                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                }
            }

            query.where(cb.and(predicates.toArray(new Predicate[0])));

            List<Order> orders = createOrderBy(cb, root, pageable);
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            }

            return entityManager.createQuery(query)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

        } catch (Exception e) {
            log.error("Erro na consulta de resultados: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<T> createResultQuery(CriteriaBuilder cb, Map<String, Object> filters, Pageable pageable) {
        CriteriaQuery<T> query = cb.createQuery(domainClass);
        Root<T> root = query.from(domainClass);

        List<Predicate> predicates = new ArrayList<>();

        addFilterPredicates(cb, root, predicates, filters);

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        List<Order> orders = createOrderBy(cb, root, pageable);
        if (!orders.isEmpty()) {
            query.orderBy(orders);
        }

        return entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    private List<Predicate> createSearchPredicates(CriteriaBuilder cb, Root<T> root, String searchText) {
        List<Predicate> searchPredicates = new ArrayList<>();
        String searchPattern = "%" + searchText.toLowerCase() + "%";

        List<String> stringFields = getStringFieldNames(domainClass);

        for (String fieldName : stringFields) {
            if (Arrays.asList("password", "token", "sensitiveData").contains(fieldName)) {
                continue;
            }

            try {
                searchPredicates.add(
                        cb.like(
                                cb.lower(root.get(fieldName).as(String.class)),
                                searchPattern
                        )
                );
            } catch (Exception e) {
                log.warn("Não foi possível adicionar predicado para o campo {}: {}", fieldName, e.getMessage());
            }
        }

        return searchPredicates;
    }

    private void addFilterPredicates(CriteriaBuilder cb, Root<T> root,
                                     List<Predicate> predicates,
                                     Map<String, Object> filters) {
        if (hasField(domainClass, "deletedAt")) {
            predicates.add(cb.isNull(root.get("deletedAt")));
        }

        if (filters != null && !filters.isEmpty()) {
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                Predicate predicate = createPredicateForField(cb, root, fieldName, value);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            }
        }
    }

    private Predicate createPredicateForField(CriteriaBuilder cb, Root<T> root,
                                              String fieldName, Object value) {
        try {
            Path<?> path = resolvePath(root, fieldName);

            if (path == null) {
                log.warn("Campo {} não encontrado na entidade {}",
                        fieldName, domainClass.getSimpleName());
                return null;
            }

            if (value == null) {
                return cb.isNull(path);
            }

            if (value instanceof String) {
                return cb.like(
                        cb.lower(path.as(String.class)),
                        "%" + ((String) value).toLowerCase() + "%"
                );
            }

            if (value instanceof List<?>) {
                return path.in((List<?>) value);
            }

            if (value instanceof Map) {
                Map<String, Object> periodMap = (Map<String, Object>) value;
                List<Predicate> predicates = new ArrayList<>();
                Object startValue = periodMap.get("startDate");
                Object endValue = periodMap.get("endDate");

                if (path.getJavaType() == LocalDateTime.class) {
                    Expression<LocalDateTime> dateTimePath = path.as(LocalDateTime.class);

                    if (startValue != null) {
                        LocalDateTime startDateTime = convertToLocalDateTime(startValue, false);
                        if (startDateTime != null) {
                            predicates.add(
                                    cb.greaterThanOrEqualTo(
                                            dateTimePath,
                                            cb.literal(startDateTime)
                                    )
                            );
                        }
                    }

                    if (endValue != null) {
                        LocalDateTime endDateTime = convertToLocalDateTime(endValue, true);
                        if (endDateTime != null) {
                            predicates.add(
                                    cb.lessThanOrEqualTo(
                                            dateTimePath,
                                            cb.literal(endDateTime)
                                    )
                            );
                        }
                    }

                    return predicates.isEmpty() ?
                            cb.conjunction() :
                            cb.and(predicates.toArray(new Predicate[0]));
                }

                if (path.getJavaType() == OffsetDateTime.class) {
                    Expression<OffsetDateTime> offsetDateTimePath = path.as(OffsetDateTime.class);

                    if (startValue != null) {
                        OffsetDateTime startDateTime = convertToOffsetDateTime(startValue, false);
                        if (startDateTime != null) {
                            predicates.add(
                                    cb.greaterThanOrEqualTo(
                                            offsetDateTimePath,
                                            cb.literal(startDateTime)
                                    )
                            );
                        }
                    }

                    if (endValue != null) {
                        OffsetDateTime endDateTime = convertToOffsetDateTime(endValue, true);
                        if (endDateTime != null) {
                            predicates.add(
                                    cb.lessThanOrEqualTo(
                                            offsetDateTimePath,
                                            cb.literal(endDateTime)
                                    )
                            );
                        }
                    }

                    return predicates.isEmpty() ?
                            cb.conjunction() :
                            cb.and(predicates.toArray(new Predicate[0]));
                }
            }

            return cb.equal(path, value);

        } catch (Exception e) {
            log.warn("Erro ao criar predicado para campo {}: {}", fieldName, e.getMessage());
            return null;
        }
    }

    private LocalDateTime convertToLocalDateTime(Object dateValue) {
        if (dateValue == null) return null;

        try {
            if (dateValue instanceof LocalDateTime) {
                return (LocalDateTime) dateValue;
            }

            if (dateValue instanceof LocalDate) {
                return ((LocalDate) dateValue).atStartOfDay();
            }

            if (dateValue instanceof String) {
                try {
                    return LocalDateTime.parse((String) dateValue);
                } catch (DateTimeParseException e1) {
                    try {
                        return LocalDate.parse((String) dateValue).atStartOfDay();
                    } catch (DateTimeParseException e2) {
                        log.warn("Formato de data não reconhecido: {}", dateValue);
                        return null;
                    }
                }
            }

            if (dateValue instanceof Number) {
                return Instant.ofEpochMilli(((Number) dateValue).longValue())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
            }

            log.warn("Tipo de data não suportado: {}", dateValue.getClass());
            return null;

        } catch (Exception e) {
            log.error("Erro ao converter data: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Converte um valor de data para LocalDateTime, com tratamento especial para endDate.
     * Se for uma data sem horário, endDate será convertida para 23:59:59 do mesmo dia.
     */
    private LocalDateTime convertToLocalDateTime(Object dateValue, boolean isEndDate) {
        if (dateValue == null) return null;

        try {
            if (dateValue instanceof LocalDateTime) {
                return (LocalDateTime) dateValue;
            }

            if (dateValue instanceof LocalDate) {
                LocalDate localDate = (LocalDate) dateValue;
                if (isEndDate) {
                    return localDate.atTime(23, 59, 59);
                } else {
                    return localDate.atStartOfDay();
                }
            }

            if (dateValue instanceof String) {
                String dateString = (String) dateValue;

                try {
                    return LocalDateTime.parse(dateString);
                } catch (DateTimeParseException e1) {
                    try {
                        LocalDate localDate = LocalDate.parse(dateString);
                        if (isEndDate) {
                            log.debug("Convertendo endDate '{}' para 23:59:59 do mesmo dia", dateString);
                            return localDate.atTime(23, 59, 59);
                        } else {
                            return localDate.atStartOfDay();
                        }
                    } catch (DateTimeParseException e2) {
                        log.warn("Formato de data não reconhecido: {}", dateValue);
                        return null;
                    }
                }
            }

            if (dateValue instanceof Number) {
                return Instant.ofEpochMilli(((Number) dateValue).longValue())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
            }

            log.warn("Tipo de data não suportado: {}", dateValue.getClass());
            return null;

        } catch (Exception e) {
            log.error("Erro ao converter data: {}", e.getMessage(), e);
            return null;
        }
    }

    private OffsetDateTime convertToOffsetDateTime(Object dateValue, boolean isEndDate) {
        if (dateValue == null) {
            return null;
        }

        try {
            if (dateValue instanceof OffsetDateTime) {
                return (OffsetDateTime) dateValue;
            }

            if (dateValue instanceof LocalDateTime) {
                return ((LocalDateTime) dateValue).atOffset(ZoneOffset.UTC);
            }

            if (dateValue instanceof LocalDate) {
                LocalDate localDate = (LocalDate) dateValue;
                if (isEndDate) {
                    return localDate.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
                }
                return localDate.atStartOfDay().atOffset(ZoneOffset.UTC);
            }

            if (dateValue instanceof Instant) {
                return ((Instant) dateValue).atOffset(ZoneOffset.UTC);
            }

            if (dateValue instanceof String) {
                String dateString = (String) dateValue;
                try {
                    return OffsetDateTime.parse(dateString);
                } catch (DateTimeParseException e1) {
                    try {
                        return LocalDateTime.parse(dateString).atOffset(ZoneOffset.UTC);
                    } catch (DateTimeParseException e2) {
                        try {
                            LocalDate localDate = LocalDate.parse(dateString);
                            if (isEndDate) {
                                return localDate.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
                            }
                            return localDate.atStartOfDay().atOffset(ZoneOffset.UTC);
                        } catch (DateTimeParseException e3) {
                            log.warn("Formato de data nao reconhecido: {}", dateValue);
                            return null;
                        }
                    }
                }
            }

            if (dateValue instanceof Number) {
                return Instant.ofEpochMilli(((Number) dateValue).longValue())
                        .atOffset(ZoneOffset.UTC);
            }

            log.warn("Tipo de data nao suportado: {}", dateValue.getClass());
            return null;
        } catch (Exception e) {
            log.error("Erro ao converter OffsetDateTime: {}", e.getMessage(), e);
            return null;
        }
    }

    private List<Order> createOrderBy(CriteriaBuilder cb, Root<T> root, Pageable pageable) {
        List<Order> orders = new ArrayList<>();

        pageable.getSort().forEach(sort -> {
            try {
                if (sort.isAscending()) {
                    orders.add(cb.asc(root.get(sort.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(sort.getProperty())));
                }
            } catch (Exception e) {
                log.warn("Não foi possível adicionar ordenação para {}: {}",
                        sort.getProperty(), e.getMessage());
            }
        });

        return orders;
    }
}
