package cn.aigestudio.downloader.bizs;

import java.util.List;

interface ITaskDAO {
    void insertTaskInfo(DLInfo info);

    void deleteTaskInfo(String url);

    void updateTaskInfo(DLInfo info);

    DLInfo queryTaskInfoByKey(String key);

    DLInfo queryTaskInfoByFileName(String name);

    List<DLInfo> queryAllTaskInfo();

    DLInfo queryTaskInfoByUrl(String url);
}