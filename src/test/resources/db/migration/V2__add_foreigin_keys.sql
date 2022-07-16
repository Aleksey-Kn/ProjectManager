ALTER TABLE project_manager.work_track
    ADD CONSTRAINT work_track_to_kanban_element FOREIGN KEY (task_id)
        REFERENCES project_manager.kanban_element(id);

ALTER TABLE project_manager.work_track
    ADD CONSTRAINT work_track_to_user FOREIGN KEY (owner_id)
        REFERENCES project_manager.user(user_id);

ALTER TABLE project_manager.user_with_project_connector
    ADD CONSTRAINT user_with_project_connector_to_custom_project_role FOREIGN KEY (project_role_id)
        REFERENCES project_manager.custom_project_role(id);

ALTER TABLE project_manager.user_with_project_connector
    ADD CONSTRAINT user_with_project_connector_to_project FOREIGN KEY (project_id)
        REFERENCES project_manager.project(id);

ALTER TABLE project_manager.user_with_project_connector
    ADD CONSTRAINT user_with_project_connector_to_user FOREIGN KEY (user_id)
        REFERENCES project_manager.user(user_id);

ALTER TABLE project_manager.user_visit_marks
    ADD UNIQUE INDEX UK_visit_marks_id(visit_marks_id);

ALTER TABLE project_manager.user_visit_marks
    ADD CONSTRAINT user_visit_marks_to_visit_mark FOREIGN KEY (visit_marks_id)
        REFERENCES project_manager.visit_mark(id);

ALTER TABLE project_manager.user_visit_marks
    ADD CONSTRAINT user_visit_marks_to_user FOREIGN KEY (user_user_id)
        REFERENCES project_manager.user(user_id);

ALTER TABLE project_manager.user_used_addresses
    ADD UNIQUE INDEX UK_used_addresses_id(used_addresses_id);

ALTER TABLE project_manager.user_used_addresses
    ADD CONSTRAINT user_used_addresses_to_used_addresses FOREIGN KEY (used_addresses_id)
        REFERENCES project_manager.used_address(id);

ALTER TABLE project_manager.user_used_addresses
    ADD CONSTRAINT user_used_addresses_to_user FOREIGN KEY (user_user_id)
        REFERENCES project_manager.user(user_id);

ALTER TABLE project_manager.user_role
    ADD CONSTRAINT user_role_to_role FOREIGN KEY (role_key)
        REFERENCES project_manager.role(id);

ALTER TABLE project_manager.user_role
    ADD CONSTRAINT user_role_to_user FOREIGN KEY (user_key)
        REFERENCES project_manager.user(user_id);

ALTER TABLE project_manager.user_notifications
    ADD UNIQUE INDEX UK_notifications_id(notifications_id);

ALTER TABLE project_manager.user_notifications
    ADD CONSTRAINT user_notifications_to_notification FOREIGN KEY (notifications_id)
        REFERENCES project_manager.notification(id);

ALTER TABLE project_manager.user_notifications
    ADD CONSTRAINT user_notifications_to_user FOREIGN KEY (user_user_id)
        REFERENCES project_manager.user(user_id);

ALTER TABLE project_manager.user_notes
    ADD UNIQUE INDEX UK_notes_id(notes_id);

ALTER TABLE project_manager.user_notes
    ADD CONSTRAINT user_notes_to_user FOREIGN KEY (user_user_id)
        REFERENCES project_manager.user(user_id);

ALTER TABLE project_manager.user_notes
    ADD CONSTRAINT user_notes_to_note FOREIGN KEY (notes_id)
        REFERENCES project_manager.note(id);

ALTER TABLE project_manager.user
    ADD UNIQUE INDEX UK_email(email);

ALTER TABLE project_manager.user
    ADD UNIQUE INDEX UK_username(username);

ALTER TABLE project_manager.tag
    ADD CONSTRAINT tag_to_kanban FOREIGN KEY (kanban_id)
        REFERENCES project_manager.kanban(id);

ALTER TABLE project_manager.statistics_using
    ADD UNIQUE INDEX UK_type(type);

ALTER TABLE project_manager.role
    ADD UNIQUE INDEX UK_name(name);

ALTER TABLE project_manager.page
    ADD CONSTRAINT page_to_user FOREIGN KEY (owner_id)
        REFERENCES project_manager.user(user_id);

ALTER TABLE project_manager.page
    ADD CONSTRAINT page_to_project FOREIGN KEY (project_id)
        REFERENCES project_manager.project(id);

ALTER TABLE project_manager.page
    ADD CONSTRAINT page_to_root FOREIGN KEY (root_id)
        REFERENCES project_manager.page(id);

ALTER TABLE project_manager.page
    ADD CONSTRAINT page_to_parent FOREIGN KEY (parent_id)
        REFERENCES project_manager.page(id);

ALTER TABLE project_manager.kanban_element_with_tag_connector
    ADD CONSTRAINT kanban_element_with_tag_connector_to_kanban_element FOREIGN KEY (kanban_element_id)
        REFERENCES project_manager.kanban_element(id);

ALTER TABLE project_manager.kanban_element_with_tag_connector
    ADD CONSTRAINT kanban_element_with_tag_connector_to_tag FOREIGN KEY (tag_id)
        REFERENCES project_manager.tag(id);

ALTER TABLE project_manager.kanban_element_comment
    ADD CONSTRAINT kanban_element_comment_to_user FOREIGN KEY (user_id)
        REFERENCES project_manager.user(user_id);

ALTER TABLE project_manager.kanban_element_comment
    ADD CONSTRAINT kanban_element_comment_to_kanban_element FOREIGN KEY (element_id)
        REFERENCES project_manager.kanban_element(id);

ALTER TABLE project_manager.kanban_element
    ADD CONSTRAINT kanban_element_owner FOREIGN KEY (owner_id)
        REFERENCES project_manager.user(user_id);

ALTER TABLE project_manager.kanban_element
    ADD CONSTRAINT kanban_element_last_redactor FOREIGN KEY (last_redactor_id)
        REFERENCES project_manager.user(user_id);

ALTER TABLE project_manager.kanban_element
    ADD CONSTRAINT kanban_element_to_kanban_column FOREIGN KEY (kanban_column_id)
        REFERENCES project_manager.kanban_column(id);

ALTER TABLE project_manager.kanban_column
    ADD CONSTRAINT kanban_column_to_kanban FOREIGN KEY (kanban_id)
        REFERENCES project_manager.kanban(id);

ALTER TABLE project_manager.kanban_attachment
    ADD CONSTRAINT kanban_attachment_to_kanban_element FOREIGN KEY (element_id)
        REFERENCES project_manager.kanban_element(id);

ALTER TABLE project_manager.kanban
    ADD CONSTRAINT kanban_to_project FOREIGN KEY (project_id)
        REFERENCES project_manager.project(id);

ALTER TABLE project_manager.custom_role_with_kanban_connector
    ADD CONSTRAINT custom_role_with_kanban_connector_to_custom_project_role FOREIGN KEY (role_id)
        REFERENCES project_manager.custom_project_role(id);

ALTER TABLE project_manager.custom_role_with_kanban_connector
    ADD CONSTRAINT custom_role_with_kanban_connector_to_kanban FOREIGN KEY (kanban_id)
        REFERENCES project_manager.kanban(id);

ALTER TABLE project_manager.custom_role_with_document_connector
    ADD CONSTRAINT custom_role_with_document_connector_to_page FOREIGN KEY (root_page_id)
        REFERENCES project_manager.page(id);

ALTER TABLE project_manager.custom_role_with_document_connector
    ADD CONSTRAINT custom_role_with_document_connector_to_custom_project_role FOREIGN KEY (role_id)
        REFERENCES project_manager.custom_project_role(id);

ALTER TABLE project_manager.custom_project_role_custom_role_with_kanban_connectors
    ADD UNIQUE INDEX UK_custom_role_with_kanban_connectors_id(custom_role_with_kanban_connectors_id);

ALTER TABLE project_manager.custom_project_role_custom_role_with_kanban_connectors
    ADD CONSTRAINT kanban_connectors_to_custom_project_role FOREIGN KEY (custom_project_role_id)
        REFERENCES project_manager.custom_project_role(id);

ALTER TABLE project_manager.custom_project_role_custom_role_with_kanban_connectors
    ADD CONSTRAINT connector_to_custom_role_with_kanban_connector FOREIGN KEY (custom_role_with_kanban_connectors_id)
        REFERENCES project_manager.custom_role_with_kanban_connector(id);

ALTER TABLE project_manager.custom_project_role_custom_role_with_document_connectors
    ADD UNIQUE INDEX UK_custom_role_with_document_connectors_id(custom_role_with_document_connectors_id);

ALTER TABLE project_manager.custom_project_role_custom_role_with_document_connectors
    ADD CONSTRAINT connectors_to_custom_role_with_document_connector FOREIGN KEY (custom_role_with_document_connectors_id)
        REFERENCES project_manager.custom_role_with_document_connector(id);

ALTER TABLE project_manager.custom_project_role_custom_role_with_document_connectors
    ADD CONSTRAINT document_connectors_to_custom_project_role FOREIGN KEY (custom_project_role_id)
        REFERENCES project_manager.custom_project_role(id);

ALTER TABLE project_manager.custom_project_role
    ADD CONSTRAINT custom_project_role_to_project FOREIGN KEY (project_id)
        REFERENCES project_manager.project(id);

ALTER TABLE project_manager.check_box
    ADD CONSTRAINT check_box_to_kanban_element FOREIGN KEY (element_id)
        REFERENCES project_manager.kanban_element(id);

ALTER TABLE project_manager.approve_action_token
    ADD CONSTRAINT approve_action_token_to_user FOREIGN KEY (user_id)
        REFERENCES project_manager.user(user_id);

ALTER TABLE project_manager.access_project
    ADD CONSTRAINT access_project_to_custom_project_role FOREIGN KEY (project_role_id)
        REFERENCES project_manager.custom_project_role(id);

ALTER TABLE project_manager.access_project
    ADD CONSTRAINT access_project_to_project FOREIGN KEY (project_id)
        REFERENCES project_manager.project(id);