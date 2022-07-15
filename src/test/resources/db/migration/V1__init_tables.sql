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

CREATE TABLE project_manager.page (
id BIGINT NOT NULL,
content LONGTEXT DEFAULT NULL,
name VARCHAR(255) NOT NULL,
published BIT(1) NOT NULL,
serial_number SMALLINT NOT NULL,
update_time BIGINT NOT NULL,
owner_id BIGINT DEFAULT NULL,
parent_id BIGINT DEFAULT NULL,
project_id BIGINT DEFAULT NULL,
root_id BIGINT DEFAULT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.project (
id BIGINT NOT NULL,
deadline VARCHAR(255) DEFAULT NULL,
description VARCHAR(255) DEFAULT NULL,
name VARCHAR(255) NOT NULL,
photo LONGBLOB DEFAULT NULL,
start_date VARCHAR(255) DEFAULT NULL,
status VARCHAR(255) DEFAULT NULL,
status_color VARCHAR(255) DEFAULT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.refresh_token (
token VARCHAR(255) NOT NULL,
login VARCHAR(255) NOT NULL,
time_to_die BIGINT DEFAULT NULL,
PRIMARY KEY (token)
);

CREATE TABLE project_manager.role (
id BIGINT NOT NULL,
name VARCHAR(255) NOT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.scheduled_mail_info (
user_email VARCHAR(255) NOT NULL,
resend BIT(1) NOT NULL,
subject VARCHAR(255) NOT NULL,
text VARCHAR(512) NOT NULL,
PRIMARY KEY (user_email)
);

CREATE TABLE project_manager.statistics_using (
id BIGINT NOT NULL,
count INT NOT NULL,
type VARCHAR(255) NOT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.tag (
id BIGINT NOT NULL,
color VARCHAR(255) DEFAULT NULL,
text VARCHAR(255) DEFAULT NULL,
kanban_id BIGINT DEFAULT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.time_remover (
remover_id BIGINT NOT NULL,
hard BIT(1) NOT NULL,
time_to_delete BIGINT NOT NULL,
PRIMARY KEY (remover_id)
);

CREATE TABLE project_manager.used_address (
id BIGINT NOT NULL,
ip VARCHAR(255) NOT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.user (
user_id BIGINT NOT NULL,
account_non_locked BIT(1) NOT NULL,
email VARCHAR(255) NOT NULL,
enabled BIT(1) NOT NULL,
last_visit BIGINT NOT NULL,
locale INT DEFAULT NULL,
nickname VARCHAR(255) NOT NULL,
password VARCHAR(255) NOT NULL,
photo LONGBLOB DEFAULT NULL,
username VARCHAR(255) NOT NULL,
zone_id INT NOT NULL,
PRIMARY KEY (user_id)
);

CREATE TABLE project_manager.user_notes (
user_user_id BIGINT NOT NULL,
notes_id BIGINT NOT NULL,
PRIMARY KEY (user_user_id, notes_id)
);

CREATE TABLE project_manager.user_notifications (
user_user_id BIGINT NOT NULL,
notifications_id BIGINT NOT NULL,
PRIMARY KEY (user_user_id, notifications_id)
);

CREATE TABLE project_manager.user_role (
user_key BIGINT NOT NULL,
role_key BIGINT NOT NULL,
PRIMARY KEY (user_key, role_key)
);

CREATE TABLE project_manager.user_used_addresses (
user_user_id BIGINT NOT NULL,
used_addresses_id BIGINT NOT NULL,
PRIMARY KEY (user_user_id, used_addresses_id)
);

CREATE TABLE project_manager.user_visit_marks (
user_user_id BIGINT NOT NULL,
visit_marks_id BIGINT NOT NULL,
PRIMARY KEY (user_user_id, visit_marks_id)
);

CREATE TABLE project_manager.user_with_project_connector (
id BIGINT NOT NULL,
role_type INT DEFAULT NULL,
project_role_id BIGINT DEFAULT NULL,
project_id BIGINT DEFAULT NULL,
user_id BIGINT DEFAULT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.visit_mark (
id BIGINT NOT NULL,
description VARCHAR(255) DEFAULT NULL,
project_id BIGINT NOT NULL,
project_name VARCHAR(255) DEFAULT NULL,
resource_id BIGINT NOT NULL,
resource_name VARCHAR(255) NOT NULL,
resource_type INT DEFAULT NULL,
serial_number INT NOT NULL,
PRIMARY KEY (id)
);

CREATE TABLE project_manager.work_track (
id BIGINT NOT NULL,
comment VARCHAR(255) NOT NULL,
work_date BIGINT NOT NULL,
work_time INT NOT NULL,
owner_id BIGINT NOT NULL,
task_id BIGINT NOT NULL,
PRIMARY KEY (id)
);