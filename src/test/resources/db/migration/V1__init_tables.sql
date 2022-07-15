CREATE TABLE project_manager.access_project (
code VARCHAR(255) NOT NULL,
disposable BIT(1) DEFAULT NULL,
time_for_die BIGINT DEFAULT NULL,
type_role_project INT DEFAULT NULL,
project_id BIGINT DEFAULT NULL,
project_role_id BIGINT DEFAULT NULL,
PRIMARY KEY (code)
);

CREATE TABLE project_manager.approve_action_token (
token VARCHAR(255) NOT NULL,
action_type INT DEFAULT NULL,
user_id BIGINT NOT NULL,
PRIMARY KEY (token)
);

CREATE TABLE project_manager.check_box (
id BIGINT NOT NULL,
is_check BIT(1) NOT NULL,
text VARCHAR(255) NOT NULL,
element_id BIGINT DEFAULT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.custom_project_role (
id BIGINT NOT NULL,
can_edit_resources BIT(1) DEFAULT NULL,
name VARCHAR(255) NOT NULL,
project_id BIGINT DEFAULT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.custom_project_role_custom_role_with_document_connectors (
custom_project_role_id BIGINT NOT NULL,
custom_role_with_document_connectors_id BIGINT NOT NULL,
PRIMARY KEY (custom_project_role_id, custom_role_with_document_connectors_id)
);

CREATE TABLE project_manager.custom_project_role_custom_role_with_kanban_connectors (
custom_project_role_id BIGINT NOT NULL,
custom_role_with_kanban_connectors_id BIGINT NOT NULL,
PRIMARY KEY (custom_project_role_id, custom_role_with_kanban_connectors_id)
);

CREATE TABLE project_manager.custom_role_with_document_connector (
id BIGINT NOT NULL,
can_edit BIT(1) NOT NULL,
root_page_id BIGINT DEFAULT NULL,
role_id BIGINT NOT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.custom_role_with_kanban_connector (
id BIGINT NOT NULL,
can_edit BIT(1) NOT NULL,
kanban_id BIGINT NOT NULL,
role_id BIGINT NOT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.kanban (
id BIGINT NOT NULL,
name VARCHAR(255) DEFAULT NULL,
photo LONGBLOB DEFAULT NULL,
project_id BIGINT DEFAULT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.kanban_attachment (
id BIGINT NOT NULL,
file_data LONGBLOB DEFAULT NULL,
filename VARCHAR(255) DEFAULT NULL,
element_id BIGINT DEFAULT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.kanban_column (
id BIGINT NOT NULL,
delayed_days INT NOT NULL,
name VARCHAR(255) NOT NULL,
serial_number INT NOT NULL,
kanban_id BIGINT DEFAULT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.kanban_element (
id BIGINT NOT NULL,
content VARCHAR(2000) DEFAULT NULL,
name VARCHAR(255) NOT NULL,
selected_date BIGINT DEFAULT NULL,
serial_number INT NOT NULL,
status INT DEFAULT NULL,
time_of_create BIGINT DEFAULT NULL,
time_of_update BIGINT DEFAULT NULL,
kanban_column_id BIGINT DEFAULT NULL,
last_redactor_id BIGINT DEFAULT NULL,
owner_id BIGINT DEFAULT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.kanban_element_comment (
id BIGINT NOT NULL,
date_time BIGINT DEFAULT NULL,
redacted BIT(1) DEFAULT NULL,
text VARCHAR(255) NOT NULL,
element_id BIGINT DEFAULT NULL,
user_id BIGINT DEFAULT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.kanban_element_with_tag_connector (
kanban_element_id BIGINT NOT NULL,
tag_id BIGINT NOT NULL,
PRIMARY KEY (kanban_element_id, tag_id)
);

CREATE TABLE project_manager.note (
id BIGINT NOT NULL,
text VARCHAR(255) NOT NULL,
user_id BIGINT NOT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.notification (
id BIGINT NOT NULL,
create_datetime BIGINT NOT NULL,
new_notification BIT(1) NOT NULL,
text VARCHAR(255) NOT NULL,
PRIMARY KEY (id)
);

