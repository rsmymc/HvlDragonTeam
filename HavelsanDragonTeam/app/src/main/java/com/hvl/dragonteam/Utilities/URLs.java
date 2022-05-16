package com.hvl.dragonteam.Utilities;

/**
 * Created by hp p on 7.5.2016.
 */
public class URLs {

    public static String urlPrefix ="https://www.uppersoftwaresolutions.com:8443/dragon/";

    public static String redisAddress = "redis://Rovingy.6878!@46.101.136.203/";

    public static String urlProfilePicture = "https://www.uppersoftwaresolutions.com/apps/dragon/img/profile_images/";

    public static String urlUploadImage = "https://www.uppersoftwaresolutions.com/apps/dragon/upload_image.php?";//TODO domain değişirse içerik değişecek

    public static String urlSendNotification = "https://www.uppersoftwaresolutions.com/apps/dragon/send_notification.php?";

    public static String urlPrivacyPolicy = "https://www.uppersoftwaresolutions.com/privacy.html";//TODO değişebilir

    //personService
    public static String urlSavePerson = urlPrefix + "personservice/save";
    public static String urlGetPerson = urlPrefix + "personservice/get";

    //trainingService
    public static String urlSaveTraining = urlPrefix + "trainingservice/save";
    public static String urlGetTrainingList = urlPrefix + "trainingservice/gettrainings";

    //attendanceService
    public static String urlGetAttendance = urlPrefix + "attendanceservice/get";
    public static String urlGetAttendanceList = urlPrefix + "attendanceservice/getattendances";
    public static String urlSaveAttendance = urlPrefix + "attendanceservice/save";
    public static String urlDeleteAttendance = urlPrefix + "attendanceservice/delete";

    //PersonTrainingAttendanceService
    public static String urlGetPersonTrainingAttendanceListByPersonNext = urlPrefix + "persontrainingattendanceservice/getpersontrainingattendancesbypersonnext";
    public static String urlGetPersonTrainingAttendanceListByPersonPast = urlPrefix + "persontrainingattendanceservice/getpersontrainingattendancesbypersonpast";
    public static String urlGetPersonTrainingAttendanceListByTraining = urlPrefix + "persontrainingattendanceservice/getpersontrainingattendancesbytraining";

    //lineupService
    public static String urlSaveLineup = urlPrefix + "lineupservice/save";
    public static String urlGetLineup = urlPrefix + "lineupservice/get";

    //announcementService
    public static String urlSaveAnnouncement = urlPrefix + "announcementservice/save";
    public static String urlGetAnnouncementList = urlPrefix + "announcementservice/getannouncements";

    //notificationService
    public static String urlGetNotification = urlPrefix + "personnotificationservice/get";
    public static String urlSaveNotification = urlPrefix + "personnotificationservice/save";
    public static String urlDeleteNotification = urlPrefix + "personnotificationservice/delete";
    public static String urlGetSinglePersonNotification = urlPrefix + "personnotificationservice/getlist";
    public static String urlGetPersonsNotification = urlPrefix + "personnotificationservice/getpersonnotifications";
}
