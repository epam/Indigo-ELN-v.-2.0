ALTER TABLE Experiment
    ADD COLUMN report_for_signature_id UUID;

ALTER TABLE Experiment
    ADD CONSTRAINT experiment_report_for_signature_id_fk FOREIGN KEY (report_for_signature_id) REFERENCES Attachment (id);
