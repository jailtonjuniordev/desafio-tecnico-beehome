CREATE TABLE tasks (
    id           CHAR(36)     NOT NULL PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    description  TEXT         NULL,
    status       VARCHAR(20)  NOT NULL,
    deadline     TIMESTAMP(6) NOT NULL,
    assigned_to  CHAR(36)     NOT  NULL,
    created_at   TIMESTAMP(6) NOT NULL,
    updated_at   TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_tasks_assigned_to FOREIGN KEY (assigned_to) REFERENCES users (id),
    CONSTRAINT uk_tasks_assigned_to_title UNIQUE (assigned_to, title)
);

CREATE INDEX idx_tasks_assigned_to_status ON tasks (assigned_to, status);
CREATE INDEX idx_tasks_deadline ON tasks (deadline);
