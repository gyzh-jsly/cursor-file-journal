#需求变更，创建新的表结构
-- 1. 监控目录表
CREATE TABLE IF NOT EXISTS monitored_directories (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dir_path VARCHAR(500) NOT NULL UNIQUE COMMENT '文件夹绝对路径',
    dir_name VARCHAR(100) COMMENT '文件夹别名',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE(活跃), ARCHIVED(已封存), LOST(目录已丢失)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    archived_at DATETIME COMMENT '封存时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控目录表';

-- 2. 文件档案表
CREATE TABLE IF NOT EXISTS dossier_files (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dir_id INT COMMENT '所属目录ID(手选文件时为NULL)',
    file_path VARCHAR(500) NOT NULL UNIQUE COMMENT '文件绝对路径',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    source_type VARCHAR(20) NOT NULL COMMENT '来源类型: WATCHED(监控目录), MANUAL(手选文件)',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE(活跃), ARCHIVED(已封存), LOCAL_DELETED(本地已删除)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '纳入管理时间',
    archived_at DATETIME COMMENT '封存时间',
    FOREIGN KEY (dir_id) REFERENCES monitored_directories(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件档案表';

-- 3. 文件修订记录表
CREATE TABLE IF NOT EXISTS file_revisions (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    file_id INT NOT NULL COMMENT '关联文件ID',
    version_number INT NOT NULL COMMENT '版本序号(1,2,3...)',
    recorded_at DATETIME NOT NULL COMMENT '修改时间(精确到秒,展示按天分组)',
    file_size BIGINT COMMENT '修改时的文件大小(字节)',
    FOREIGN KEY (file_id) REFERENCES dossier_files(id) ON DELETE CASCADE,
    INDEX idx_file_time (file_id, recorded_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件修订记录表';

-- 4. 文件备注表
CREATE TABLE IF NOT EXISTS file_notes (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    file_id INT NOT NULL COMMENT '关联文件ID',
    revision_id INT COMMENT '关联修订记录ID(文件级备注时为NULL)',
    content TEXT NOT NULL COMMENT '备注内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '备注时间',
    FOREIGN KEY (file_id) REFERENCES dossier_files(id) ON DELETE CASCADE,
    FOREIGN KEY (revision_id) REFERENCES file_revisions(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件备注表';