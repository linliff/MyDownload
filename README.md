# MyDownload

Android 封装下载模块，包括系统下载和自定义HTTP下载
使用时可以在判断手机是否支持系统下载后选择采用哪种下载方式

判断是否支持系统下载的代码：

    public static boolean isDownloadManagerAvailable(Context context) {

        String downloadPackage = "com.android.providers.downloads";

        if (PackageUtil.getInstallFlags(context, downloadPackage)) {
            PackageManager pm = context.getPackageManager();
            int enable = pm.getApplicationEnabledSetting(downloadPackage);
            if (enable == 0 || enable == 1) {
                return true;
            }
        }

        return false;

    }


下载模块对外暴露调用接口可以看demo

HTTP下载功能包括：
正常下载、暂停下载、断点续传、多线程下载
