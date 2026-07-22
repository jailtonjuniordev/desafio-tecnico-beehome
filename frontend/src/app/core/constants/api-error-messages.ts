const METHOD_NOT_SUPPORTED_PREFIX = 'Method not supported:';
const METHOD_NOT_SUPPORTED_PREFIX_PT = 'Metodo não suportado:';

export const API_ERROR_MESSAGES: Record<string, string> = {
  'Task not found': 'Tarefa não encontrada',
  'Task title already exists': 'Já existe uma tarefa com este título',
  'Deadline cannot be in the past': 'O prazo não pode estar no passado',
  'deadlineStart must be before or equal to deadlineEnd': 'Prazo Inicial deve ser antes ou igual ao Prazo Final',
  'User not found': 'Usuario não encontrado',
  'This email are already registered, try login into your account': 'Este e-mail já está cadastrado. Tente entrar na sua conta',
  'Email already registered': 'E-mail já cadastrado',
  'Email or password incorrect': 'E-mail ou senha incorretos',
  'Validation failed': 'Falha na validação',
  'Authentication required': 'Autenticação necessária',
  'Access denied': 'Acesso negado',
};

export function translateApiErrorMessage(message: string): string {
  const translated = API_ERROR_MESSAGES[message];
  if (translated) {
    return translated;
  }

  if (message.startsWith(METHOD_NOT_SUPPORTED_PREFIX)) {
    return (
      METHOD_NOT_SUPPORTED_PREFIX_PT + message.slice(METHOD_NOT_SUPPORTED_PREFIX.length)
    );
  }

  return message;
}
