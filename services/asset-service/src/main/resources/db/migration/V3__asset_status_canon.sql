UPDATE assets
SET status = CASE
    WHEN status = 'RETIRED' THEN 'RETIRED'
    ELSE 'ACTIVE'
END;

ALTER TABLE assets
    ADD CONSTRAINT chk_assets_status_canon
    CHECK (status IN ('ACTIVE', 'RETIRED'));
