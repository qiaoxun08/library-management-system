package com.library.entity;

import java.time.LocalDateTime;

/**
 * 权限实体
 */
public class SysPermission {
    private Integer id;
    private String permKey;
    private String permName;
    private String module;
    private String description;
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getPermKey() { return permKey; }
    public void setPermKey(String permKey) { this.permKey = permKey; }
    public String getPermName() { return permName; }
    public void setPermName(String permName) { this.permName = permName; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
