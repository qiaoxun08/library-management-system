package com.library.dto;

import java.time.LocalDateTime;

/**
 * 读者公开主页信息（白名单字段）
 * 仅包含可对外公开展示的字段，绝不包含 phone/email/罚款/借阅数/密码等隐私
 */
public class PublicReaderProfile {

    private Integer id;
    private String readerId;
    private String realName;
    private Integer gender; // 0-女 1-男
    private String department;
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
