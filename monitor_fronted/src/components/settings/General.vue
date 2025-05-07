<template>
    <div>
        <form class="my-4" autocomplete="off" @submit.prevent="saveGeneral">
            <!-- Current Password -->
            <div class="mb-4">
                <label for="currentPassword" class="form-label small text-secondary mb-1">
                    {{ $t("当前密码") }}
                </label>
                <div class="password-field">
                    <input
                        id="currentPassword"
                        v-model="currentPassword"
                        :type="showCurrentPassword ? 'text' : 'password'"
                        class="form-control"
                        autocomplete="current-password"
                        required
                    />
                    <div
                        class="toggle-icon"
                        @click="showCurrentPassword = !showCurrentPassword"
                    >
                        <font-awesome-icon :icon="showCurrentPassword ? 'eye-slash' : 'eye'" />
                    </div>
                </div>
            </div>

            <!-- New Password -->
            <div class="mb-4">
                <label for="newPassword" class="form-label small text-secondary mb-1">
                    {{ $t("修改后密码") }}
                </label>
                <div class="password-field">
                    <input
                        id="newPassword"
                        v-model="newPassword"
                        :type="showNewPassword ? 'text' : 'password'"
                        class="form-control"
                        autocomplete="new-password"
                        required
                    />
                    <div
                        class="toggle-icon"
                        @click="showNewPassword = !showNewPassword"
                    >
                        <font-awesome-icon :icon="showNewPassword ? 'eye-slash' : 'eye'" />
                    </div>
                </div>
            </div>

            <!-- Search Engine -->
<!--            <div class="mb-4">-->
<!--                <label class="form-label">-->
<!--                    {{ $t("Search Engine Visibility") }}-->
<!--                </label>-->

<!--                <div class="form-check">-->
<!--                    <input-->
<!--                        id="searchEngineIndexYes"-->
<!--                        v-model="settings.searchEngineIndex"-->
<!--                        class="form-check-input"-->
<!--                        type="radio"-->
<!--                        name="searchEngineIndex"-->
<!--                        :value="true"-->
<!--                        required-->
<!--                    />-->
<!--                    <label class="form-check-label" for="searchEngineIndexYes">-->
<!--                        {{ $t("Allow indexing") }}-->
<!--                    </label>-->
<!--                </div>-->
<!--                <div class="form-check">-->
<!--                    <input-->
<!--                        id="searchEngineIndexNo"-->
<!--                        v-model="settings.searchEngineIndex"-->
<!--                        class="form-check-input"-->
<!--                        type="radio"-->
<!--                        name="searchEngineIndex"-->
<!--                        :value="false"-->
<!--                        required-->
<!--                    />-->
<!--                    <label class="form-check-label" for="searchEngineIndexNo">-->
<!--                        {{ $t("Discourage search engines from indexing site") }}-->
<!--                    </label>-->
<!--                </div>-->
<!--            </div>-->

            <!-- Entry Page -->
<!--            <div class="mb-4">-->
<!--                <label class="form-label">{{ $t("Entry Page") }}</label>-->

<!--                <div class="form-check">-->
<!--                    <input-->
<!--                        id="entryPageDashboard"-->
<!--                        v-model="settings.entryPage"-->
<!--                        class="form-check-input"-->
<!--                        type="radio"-->
<!--                        name="entryPage"-->
<!--                        value="dashboard"-->
<!--                        required-->
<!--                    />-->
<!--                    <label class="form-check-label" for="entryPageDashboard">-->
<!--                        {{ $t("Dashboard") }}-->
<!--                    </label>-->
<!--                </div>-->

<!--                <div v-for="statusPage in $root.statusPageList" :key="statusPage.id" class="form-check">-->
<!--                    <input-->
<!--                        :id="'status-page-' + statusPage.id"-->
<!--                        v-model="settings.entryPage"-->
<!--                        class="form-check-input"-->
<!--                        type="radio"-->
<!--                        name="entryPage"-->
<!--                        :value="'statusPage-' + statusPage.slug"-->
<!--                        required-->
<!--                    />-->
<!--                    <label class="form-check-label" :for="'status-page-' + statusPage.id">-->
<!--                        {{ $t("Status Page") }} - {{ statusPage.title }}-->
<!--                    </label>-->
<!--                </div>-->
<!--            </div>-->

            <!-- Primary Base URL -->
<!--            <div class="mb-4">-->
<!--                <label class="form-label" for="primaryBaseURL">-->
<!--                    {{ $t("Primary Base URL") }}-->
<!--                </label>-->

<!--                <div class="input-group mb-3">-->
<!--                    <input-->
<!--                        id="primaryBaseURL"-->
<!--                        v-model="settings.primaryBaseURL"-->
<!--                        class="form-control"-->
<!--                        name="primaryBaseURL"-->
<!--                        placeholder="https://"-->
<!--                        pattern="https?://.+"-->
<!--                        autocomplete="new-password"-->
<!--                    />-->
<!--                    <button class="btn btn-outline-primary" type="button" @click="autoGetPrimaryBaseURL">-->
<!--                        {{ $t("Auto Get") }}-->
<!--                    </button>-->
<!--                </div>-->

<!--                <div class="form-text"></div>-->
<!--            </div>-->

            <!-- Steam API Key -->
<!--            <div class="mb-4">-->
<!--                <label class="form-label" for="steamAPIKey">-->
<!--                    {{ $t("Steam API Key") }}-->
<!--                </label>-->
<!--                <HiddenInput-->
<!--                    id="steamAPIKey"-->
<!--                    v-model="settings.steamAPIKey"-->
<!--                    autocomplete="new-password"-->
<!--                />-->
<!--                <div class="form-text">-->
<!--                    {{ $t("steamApiKeyDescription") }}-->
<!--                    <a href="https://steamcommunity.com/dev" target="_blank">-->
<!--                        https://steamcommunity.com/dev-->
<!--                    </a>-->
<!--                </div>-->
<!--            </div>-->

<!--            &lt;!&ndash; DNS Cache (nscd) &ndash;&gt;-->
<!--            <div v-if="$root.info.isContainer" class="mb-4">-->
<!--                <label class="form-label">-->
<!--                    {{ $t("enableNSCD") }}-->
<!--                </label>-->

<!--                <div class="form-check">-->
<!--                    <input-->
<!--                        id="nscdEnable"-->
<!--                        v-model="settings.nscd"-->
<!--                        class="form-check-input"-->
<!--                        type="radio"-->
<!--                        name="nscd"-->
<!--                        :value="true"-->
<!--                        required-->
<!--                    />-->
<!--                    <label class="form-check-label" for="nscdEnable">-->
<!--                        {{ $t("Enable") }}-->
<!--                    </label>-->
<!--                </div>-->

<!--                <div class="form-check">-->
<!--                    <input-->
<!--                        id="nscdDisable"-->
<!--                        v-model="settings.nscd"-->
<!--                        class="form-check-input"-->
<!--                        type="radio"-->
<!--                        name="nscd"-->
<!--                        :value="false"-->
<!--                        required-->
<!--                    />-->
<!--                    <label class="form-check-label" for="nscdDisable">-->
<!--                        {{ $t("Disable") }}-->
<!--                    </label>-->
<!--                </div>-->
<!--            </div>-->

<!--            &lt;!&ndash; DNS Cache &ndash;&gt;-->
<!--            <div class="mb-4">-->
<!--                <label class="form-label">-->
<!--                    {{ $t("Enable DNS Cache") }}-->
<!--                    <div class="form-text">-->
<!--                        ⚠️ {{ $t("dnsCacheDescription") }}-->
<!--                    </div>-->
<!--                </label>-->

<!--                <div class="form-check">-->
<!--                    <input-->
<!--                        id="dnsCacheEnable"-->
<!--                        v-model="settings.dnsCache"-->
<!--                        class="form-check-input"-->
<!--                        type="radio"-->
<!--                        name="dnsCache"-->
<!--                        :value="true"-->
<!--                        required-->
<!--                    />-->
<!--                    <label class="form-check-label" for="dnsCacheEnable">-->
<!--                        {{ $t("Enable") }}-->
<!--                    </label>-->
<!--                </div>-->

<!--                <div class="form-check">-->
<!--                    <input-->
<!--                        id="dnsCacheDisable"-->
<!--                        v-model="settings.dnsCache"-->
<!--                        class="form-check-input"-->
<!--                        type="radio"-->
<!--                        name="dnsCache"-->
<!--                        :value="false"-->
<!--                        required-->
<!--                    />-->
<!--                    <label class="form-check-label" for="dnsCacheDisable">-->
<!--                        {{ $t("Disable") }}-->
<!--                    </label>-->
<!--                </div>-->
<!--            </div>-->

            <!-- Chrome Executable -->
<!--            <div class="mb-4">-->
<!--                <label class="form-label" for="primaryBaseURL">-->
<!--                    {{ $t("chromeExecutable") }}-->
<!--                </label>-->

<!--                <div class="input-group mb-3">-->
<!--                    <input-->
<!--                        id="primaryBaseURL"-->
<!--                        v-model="settings.chromeExecutable"-->
<!--                        class="form-control"-->
<!--                        name="primaryBaseURL"-->
<!--                        :placeholder="$t('chromeExecutableAutoDetect')"-->
<!--                    />-->
<!--                    <button class="btn btn-outline-primary" type="button" @click="testChrome">-->
<!--                        {{ $t("Test") }}-->
<!--                    </button>-->
<!--                </div>-->

<!--                <div class="form-text">-->
<!--                    {{ $t("chromeExecutableDescription") }}-->
<!--                </div>-->
<!--            </div>-->

            <!-- Save Button -->
            <div>
                <button class="btn btn-primary" type="submit">
                    {{ $t("Save") }}
                </button>
            </div>
        </form>
    </div>
</template>

<script>
import HiddenInput from "../../components/HiddenInput.vue";
import dayjs from "dayjs";
import { timezoneList } from "../../util-frontend";

export default {
    components: {
        HiddenInput,
    },

    data() {
        return {
            timezoneList: timezoneList(),
            currentPassword: "",
            newPassword: "",
            showCurrentPassword: false,
            showNewPassword: false,
        };
    },

    computed: {
        settings() {
            return this.$parent.$parent.$parent.settings;
        },
        saveSettings() {
            this.$root.getSocket().emit("setSettings", [this.currentPassword, this.newPassword], (res) => {
                this.$root.toastRes(res);
            });
        },
        settingsLoaded() {
            return this.$parent.$parent.$parent.settingsLoaded;
        },
        guessTimezone() {
            return dayjs.tz.guess();
        }
    },

    methods: {
        /** Save the settings */
        saveGeneral() {
            localStorage.timezone = this.$root.userTimezone;
            this.saveSettings();
        },
        /** Get the base URL of the application */
        autoGetPrimaryBaseURL() {
            this.settings.primaryBaseURL = "http://localhost:3000";
        },

        testChrome() {
            this.$root.getSocket().emit("testChrome", this.settings.chromeExecutable, (res) => {
                this.$root.toastRes(res);
            });
        },
    },
};
</script>
<style lang="scss" scoped>
.password-field {
    position: relative;

    .form-control {
        height: 42px;
        border-radius: 21px;
        padding-right: 40px;
        padding-left: 16px;
        transition: all 0.2s ease;
        border: 1px solid #ced4da;

        &:focus {
            box-shadow: 0 0 0 0.15rem rgba(124, 232, 164, 0.35);
            border-color: #7ce8a4;
            outline: none;
        }
    }

    .toggle-icon {
        position: absolute;
        right: 16px;
        top: 50%;
        transform: translateY(-50%);
        color: #8e9aaf;
        cursor: pointer;
        font-size: 0.9rem;
        z-index: 5;
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: color 0.15s ease;

        &:hover {
            color: #495057;
        }
    }
}

.dark .password-field {
    .form-control {
        background-color: #2b3035;
        border-color: #495057;
        color: #e9ecef;

        &:focus {
            box-shadow: 0 0 0 0.15rem rgba(124, 232, 164, 0.25);
            border-color: #7ce8a4;
        }
    }

    .toggle-icon {
        color: #adb5bd;

        &:hover {
            color: #e9ecef;
        }
    }
}
</style>
