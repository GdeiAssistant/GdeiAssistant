-- Apply to the configured data database before deploying the new announcement unread endpoints.
CREATE TABLE IF NOT EXISTS announcement_read (
  username VARCHAR(24) COLLATE utf8mb4_bin NOT NULL,
  announcement_id INT NOT NULL,
  PRIMARY KEY (username, announcement_id),
  CONSTRAINT fk_announcement_read FOREIGN KEY (announcement_id) REFERENCES announcement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
