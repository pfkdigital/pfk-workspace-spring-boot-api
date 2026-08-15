CREATE TABLE tasks
(
    id               UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    project_id       UUID         NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    workspace_id     UUID         NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,

    title            VARCHAR(255) NOT NULL,
    description      TEXT,
    status           VARCHAR(50)  NOT NULL DEFAULT 'TODO'
        CHECK (status IN ('TODO', 'IN_PROGRESS', 'REVIEW', 'DONE')),
    priority         VARCHAR(50)  NOT NULL DEFAULT 'MEDIUM'
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),

    assignee_user_id UUID         REFERENCES users (id) ON DELETE SET NULL,
    due_date         DATE,

    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at     TIMESTAMP
);
CREATE INDEX tasks_project_id_idx ON tasks (project_id);
CREATE INDEX tasks_workspace_id_idx ON tasks (workspace_id);
CREATE INDEX tasks_assignee_user_id_idx ON tasks (assignee_user_id);
CREATE INDEX tasks_status_idx ON tasks (status);

CREATE TABLE subtasks
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    task_id    UUID         NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,

    title      VARCHAR(255) NOT NULL,
    done       BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX subtasks_task_id_idx ON subtasks (task_id);

CREATE TABLE labels
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    project_id   UUID         NOT NULL REFERENCES projects (id) ON DELETE CASCADE,

    name         VARCHAR(100) NOT NULL,
    color        VARCHAR(20)  NOT NULL,

    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, name)
);
CREATE INDEX labels_project_id_idx ON labels (project_id);

CREATE TABLE task_labels
(
    task_id  UUID NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    label_id UUID NOT NULL REFERENCES labels (id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, label_id)
);
CREATE INDEX task_labels_label_id_idx ON task_labels (label_id);

CREATE TABLE attachments
(
    id                  UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    task_id             UUID         NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    project_id          UUID         NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    workspace_id        UUID         NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,

    filename            VARCHAR(255) NOT NULL,
    content_type        VARCHAR(255) NOT NULL,
    extension           VARCHAR(20)  NOT NULL,
    size_bytes          BIGINT       NOT NULL CHECK (size_bytes >= 0),
    checksum            VARCHAR(255) NOT NULL,

    storage_key         VARCHAR(500) NOT NULL UNIQUE,

    uploaded_by_user_id UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,

    status              VARCHAR(50)  NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PROCESSING', 'READY', 'FAILED')),

    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX attachments_task_id_idx ON attachments (task_id);
CREATE INDEX attachments_project_id_idx ON attachments (project_id);
CREATE INDEX attachments_workspace_id_idx ON attachments (workspace_id);
CREATE INDEX attachments_uploaded_by_user_id_idx ON attachments (uploaded_by_user_id);

CREATE TABLE comments
(
    id             UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    task_id        UUID      NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    author_user_id UUID      NOT NULL REFERENCES users (id) ON DELETE CASCADE,

    body           TEXT      NOT NULL,

    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edited_at      TIMESTAMP
);
CREATE INDEX comments_task_id_idx ON comments (task_id);
CREATE INDEX comments_author_user_id_idx ON comments (author_user_id);
