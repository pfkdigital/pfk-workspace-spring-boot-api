CREATE TABLE workspaces
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    image_url     VARCHAR(255),
    owner_user_id UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX workspace_owner_user_id_idx ON workspaces (owner_user_id);

CREATE TABLE workspace_members
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    workspace_id UUID        NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role         VARCHAR(50) NOT NULL CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER')),
    joined_at    TIMESTAMP   NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (workspace_id, user_id)
);
CREATE INDEX workspace_members_workspace_id_idx ON workspace_members (workspace_id);
CREATE INDEX workspace_members_user_id_idx ON workspace_members (user_id);
CREATE INDEX workspace_members_role_idx ON workspace_members (role);

CREATE TABLE workspace_invitations
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    workspace_id UUID         NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    email        VARCHAR(255) NOT NULL,
    role         VARCHAR(50)  NOT NULL,
    token_hash   VARCHAR(255) NOT NULL UNIQUE,
    expires_at   TIMESTAMP    NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status       VARCHAR(50)      NOT NULL CHECK ( status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    is_used      BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (token_hash)
);
CREATE INDEX workspace_invitations_workspace_id_idx ON workspace_invitations (workspace_id);
CREATE INDEX workspace_invitations_email_idx ON workspace_invitations (email);
CREATE INDEX workspace_invitations_token_hash_idx ON workspace_invitations (token_hash);