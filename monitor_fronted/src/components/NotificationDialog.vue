<template>
    <form @submit.prevent="submit">
        <div ref="modal" class="modal fade" tabindex="-1" data-bs-backdrop="static">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 id="exampleModalLabel" class="modal-title">
                            {{ $t("Setup Notification") }}
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close" />
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label for="notification-type" class="form-label">{{ $t("Notification Type") }}</label>
                            <select id="notification-type" v-model="notification.type" class="form-select">
                                <option v-for="(name, type) in notificationNameList.regularList" :key="type" :value="type">{{ name }}</option>
                                <optgroup :label="$t('notificationRegional')">
                                    <option v-for="(name, type) in notificationNameList.regionalList" :key="type" :value="type">{{ name }}</option>
                                </optgroup>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label for="notification-name" class="form-label">{{ $t("Friendly Name") }}</label>
                            <input id="notification-name" v-model="notification.name" type="text" class="form-control" required>
                        </div>

                        <!-- form body -->
                        <component :is="currentForm" />

                        <div class="mb-3 mt-4">
                            <hr class="dropdown-divider mb-4">

<!--                            <div class="form-check form-switch">-->
<!--                                <input v-model="notification.isDefault" class="form-check-input" type="checkbox">-->
<!--                                <label class="form-check-label">{{ $t("Default enabled") }}</label>-->
<!--                            </div>-->
<!--                            <div class="form-text">-->
<!--                                {{ $t("enableDefaultNotificationDescription") }}-->
<!--                            </div>-->

<!--                            <br>-->

<!--                            <div class="form-check form-switch">-->
<!--                                <input v-model="notification.applyExisting" class="form-check-input" type="checkbox">-->
<!--                                <label class="form-check-label">{{ $t("Apply on all existing monitors") }}</label>-->
<!--                            </div>-->
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button v-if="id" type="button" class="btn btn-danger" :disabled="processing" @click="deleteConfirm">
                            {{ $t("Delete") }}
                        </button>
                        <button type="button" class="btn btn-warning" :disabled="processing" @click="test">
                            {{ $t("Test") }}
                        </button>
                        <button type="submit" class="btn btn-primary" :disabled="processing">
                            <div v-if="processing" class="spinner-border spinner-border-sm me-1"></div>
                            {{ $t("Save") }}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </form>

    <Confirm ref="confirmDelete" btn-style="btn-danger" :yes-text="$t('Yes')" :no-text="$t('No')" @yes="deleteNotification">
        {{ $t("deleteNotificationMsg") }}
    </Confirm>
</template>

<script>
import { Modal } from "bootstrap";

import Confirm from "./Confirm.vue";
import NotificationFormList from "./notifications";

export default {
    components: {
        Confirm,
    },
    props: {},
    emits: [ "added" ],
    data() {
        return {
            model: null,
            processing: false,
            id: null,
            notificationTypes: Object.keys(NotificationFormList).sort((a, b) => {
                return a.toLowerCase().localeCompare(b.toLowerCase());
            }),
            notification: {
                name: "",
                /** @type { null | keyof NotificationFormList } */
                type: null,
                isDefault: false,
                email: "",
                subject: ""
                // Do not set default value here, please scroll to show()
            }
        };
    },

    computed: {
        currentForm() {
            return NotificationFormList["smtp"];
        },

        notificationNameList() {
            let regularList = {
                // "alerta": "Alerta",
                // "AlertNow": "AlertNow",
                // "apprise": this.$t("apprise"),
                // "Bark": "Bark",
                // "clicksendsms": "ClickSend SMS",
                // "discord": "Discord",
                // "GoogleChat": "Google Chat (Google Workspace)",
                // "gorush": "Gorush",
                // "gotify": "Gotify",
                // "HomeAssistant": "Home Assistant",
                // "Kook": "Kook",
                // "line": "LINE Messenger",
                // "LineNotify": "LINE Notify",
                // "lunasea": "LunaSea",
                // "matrix": "Matrix",
                // "mattermost": "Mattermost",
                // "nostr": "Nostr",
                // "ntfy": "Ntfy",
                // "octopush": "Octopush",
                // "OneBot": "OneBot",
                // "Opsgenie": "Opsgenie",
                // "PagerDuty": "PagerDuty",
                // "PagerTree": "PagerTree",
                // "pushbullet": "Pushbullet",
                // "PushByTechulus": "Push by Techulus",
                // "pushover": "Pushover",
                // "pushy": "Pushy",
                // "rocket.chat": "Rocket.Chat",
                // "signal": "Signal",
                // "slack": "Slack",
                // "squadcast": "SquadCast",
                // "SMSEagle": "SMSEagle",
                "smtp": this.$t("smtp"),
                // "stackfield": "Stackfield",
                // "teams": "Microsoft Teams",
                // "telegram": "Telegram",
                // "twilio": "Twilio",
                // "Splunk": "Splunk",
                // "webhook": "Webhook",
                // "GoAlert": "GoAlert",
                // "ZohoCliq": "ZohoCliq"
            };

            // Put notifications here if it's not supported in most regions or its documentation is not in English
            let regionalList = {
                // "AliyunSMS": "AliyunSMS (阿里云短信服务)",
                // "DingDing": "DingDing (钉钉自定义机器人)",
                // "Feishu": "Feishu (飞书)",
                // "FlashDuty": "FlashDuty (快猫星云)",
                // "FreeMobile": "FreeMobile (mobile.free.fr)",
                // "PushDeer": "PushDeer",
                // "promosms": "PromoSMS",
                // "serwersms": "SerwerSMS.pl",
                // "SMSManager": "SmsManager (smsmanager.cz)",
                // "WeCom": "WeCom (企业微信群机器人)",
                // "ServerChan": "ServerChan (Server酱)",
                // "smsc": "SMSC",
            };

            // Sort by notification name
            // No idea how, but it works
            // https://stackoverflow.com/questions/1069666/sorting-object-property-by-values
            let sort = (list2) => {
                return Object.entries(list2)
                    .sort(([ , a ], [ , b ]) => a.localeCompare(b))
                    .reduce((r, [ k, v ]) => ({
                        ...r,
                        [k]: v
                    }), {});
            };

            return {
                regularList: sort(regularList),
                regionalList: sort(regionalList),
            };
        },

        notificationFullNameList() {
            let list = {};
            for (let [ key, value ] of Object.entries(this.notificationNameList.regularList)) {
                list[key] = value;
            }
            for (let [ key, value ] of Object.entries(this.notificationNameList.regionalList)) {
                list[key] = value;
            }
            return list;
        },
    },

    watch: {
        "notification.type"(to, from) {
            let oldName;
            if (from) {
                oldName = this.getUniqueDefaultName(from);
            } else {
                oldName = "";
            }

            if (! this.notification.name || this.notification.name === oldName) {
                this.notification.name = this.getUniqueDefaultName(to);
            }
        },
    },
    mounted() {
        this.modal = new Modal(this.$refs.modal);
    },
    methods: {

        /** Show dialog to confirm deletion */
        deleteConfirm() {
            this.modal.hide();
            this.$refs.confirmDelete.show();
        },

        /**
         * Show settings for specified notification
         * @param {number} notificationID ID of notification to show
         */
        show(notificationID) {
            if (notificationID) {
                this.id = notificationID;

                for (let n of this.$root.notificationList) {
                    if (n.id === notificationID) {
                        this.notification = n;
                        break;
                    }
                }
            } else {
                this.id = null;
                this.notification = {
                    name: "",
                    type: "smtp",
                    isDefault: false,
                };
            }

            this.modal.show();
        },

        /** Submit the form to the server */
        submit() {
            this.processing = true;
            this.$root.getSocket().emit("addNotification", this.notification, this.id, (res) => {
                this.$root.toastRes(res);
                this.processing = false;

                if (res.ok) {
                    this.modal.hide();

                    // Emit added event, doesn't emit edit.
                    if (! this.id) {
                        this.$emit("added", res.id);
                    }

                }
            });
        },

        /** Test the notification endpoint */
        test() {
            this.processing = true;
            this.$root.getSocket().emit("testNotification", this.notification, (res) => {
                this.$root.toastRes(res);
                this.processing = false;
            });
        },

        /** Delete the notification endpoint */
        deleteNotification() {
            this.processing = true;
            this.$root.getSocket().emit("deleteNotification", this.id, (res) => {
                this.$root.toastRes(res);
                this.processing = false;

                if (res.ok) {
                    this.modal.hide();
                }
            });
        },
        /**
         * Get a unique default name for the notification
         * @param {keyof NotificationFormList} notificationKey
         * @return {string}
         */
        getUniqueDefaultName(notificationKey) {

            let index = 1;
            let name = "";
            do {
                name = this.$t("defaultNotificationName", {
                    notification: this.notificationFullNameList[notificationKey].replace(/\(.+\)/, "").trim(),
                    number: index++
                });
            } while (this.$root.notificationList.find(it => it.name === name));
            return name;
        }
    },
};
</script>

<style lang="scss" scoped>
@import "../assets/vars.scss";

.dark {
    .modal-dialog .form-text, .modal-dialog p {
        color: $dark-font-color;
    }
}
</style>
