package edu.gdei.gdeiassistant.Repository.Mongodb.Announcement;

import edu.gdei.gdeiassistant.Pojo.Entity.Announcement;

public interface AnnouncementDao {

    public Announcement QueryLatestAnnouncement();

    public void InsertAnnouncement(Announcement announcement);
}
