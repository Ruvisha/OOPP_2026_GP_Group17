package com.fot.ams.service;

import com.fot.ams.dao.NoticeDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Notice;
import com.fot.ams.util.ValidationUtil;

import java.util.List;

/**
 * Service managing notices and announcements.
 */
public class NoticeService {
    private final NoticeDAO noticeDAO;

    public NoticeService() {
        this.noticeDAO = new NoticeDAO();
    }

    public NoticeService(NoticeDAO noticeDAO) {
        this.noticeDAO = noticeDAO;
    }

    public List<Notice> getAllNotices() {
        return noticeDAO.getAllNotices();
    }

    public List<Notice> getActiveNotices() {
        return noticeDAO.getActiveNotices();
    }

    public boolean addNotice(Notice notice) throws ValidationException {
        ValidationUtil.validateNotEmpty(notice.getTitle(), "Notice Title");
        ValidationUtil.validateNotEmpty(notice.getContent(), "Notice Content");
        if (notice.getPublishedDate() == null) {
            throw new ValidationException("Published date is required.");
        }
        return noticeDAO.addNotice(notice);
    }

    public boolean updateNotice(Notice notice) throws ValidationException {
        ValidationUtil.validateNotEmpty(notice.getTitle(), "Notice Title");
        ValidationUtil.validateNotEmpty(notice.getContent(), "Notice Content");
        return noticeDAO.updateNotice(notice);
    }

    public boolean deleteNotice(int noticeId) {
        return noticeDAO.deleteNotice(noticeId);
    }
}
