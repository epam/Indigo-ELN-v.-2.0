DROP TABLE Signature_Template_Block;
DROP TABLE Signature_Template;

ALTER TABLE Experiment DROP COLUMN report_for_signature_id CASCADE;
ALTER TABLE Experiment ADD COLUMN signature_number VARCHAR(1024);
ALTER TABLE Experiment ADD COLUMN signature_attachment_id UUID;
ALTER TABLE Experiment ADD CONSTRAINT experiment_signature_attachment_id_fk FOREIGN KEY (signature_attachment_id) REFERENCES Attachment (id) ON DELETE SET NULL;
DROP TABLE Experiment_Signature;
