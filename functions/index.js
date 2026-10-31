const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendNotificationOnNewCampaign = onDocumentCreated("Campaigns/{campaignId}", async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
        console.log("Không có dữ liệu trong sự kiện.");
        return null;
    }

    const campaignData = snapshot.data();
    const title = "Bài đăng giải cứu mới!";
    const body = `Có chiến dịch: ${campaignData.title || "Nông sản"} tại ${campaignData.address || "Địa điểm mới"}`;

    try {
        const usersSnapshot = await admin.firestore().collection("users").get();
        const tokens = [];

        usersSnapshot.forEach((doc) => {
            const userData = doc.data();
            if (userData.fcmToken) {
                tokens.push(userData.fcmToken);
            }
        });

        if (tokens.length === 0) {
            console.log("Không tìm thấy FCM Token nào trên hệ thống.");
            return null;
        }

        const payload = {
            notification: {
                title: title,
                body: body,
                sound: "default",
            },
            data: {
                campaignId: event.params.campaignId,
            },
        };

        const response = await admin.messaging().sendToDevice(tokens, payload);
        console.log("Gửi thông báo thành công:", response);
        return null;
    } catch (error) {
        console.error("Lỗi khi gửi thông báo:", error);
        return null;
    }
});