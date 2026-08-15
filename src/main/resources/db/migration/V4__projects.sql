CREATE TABLE projects
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    workspace_id       UUID         NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    color              VARCHAR(20),
    status             VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE', 'ARCHIVED', 'COMPLETED')),
    start_date         DATE,
    target_date        DATE,
    archived_at        TIMESTAMP,
    created_by_user_id UUID                  REFERENCES users (id) ON DELETE SET NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (workspace_id)
);
CREATE INDEX projects_workspace_id_idx ON projects (workspace_id);
CREATE INDEX projects_created_by_user_id_idx ON projects (created_by_user_id);

CREATE TABLE project_links
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    project_id UUID          NOT NULL REFERENCES projects (id) ON DELETE CASCADE,

    label      VARCHAR(255)  NOT NULL,
    url        VARCHAR(1000) NOT NULL,
    icon       VARCHAR(50),

    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX project_links_project_id_idx ON project_links (project_id);
