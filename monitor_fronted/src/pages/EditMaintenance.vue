<template>
    <transition name="slide-fade" appear>
        <div>
            <h1 class="mb-3">{{ pageName }}</h1>
            <form @submit.prevent="submit">
                <div class="shadow-box shadow-box-with-fixed-bottom-bar">
                    <div class="row">
                        <div class="col-xl-10">
                            <!-- Title -->
                            <div class="mb-3">
                                <label for="name" class="form-label">{{ $t("Title") }}</label>
                                <input
                                    id="name" v-model="maintenance.title" type="text" class="form-control"
                                    required
                                >
                            </div>

                            <!-- Description -->
                            <div class="my-3">
                                <label for="description" class="form-label">{{ $t("Description") }}</label>
                                <textarea
                                    id="description" v-model="maintenance.description" class="form-control"
                                ></textarea>
                                <div class="form-text">
                                    {{ $t("markdownSupported") }}
                                </div>
                            </div>

                            <!-- Affected Monitors -->
                            <h2 class="mt-5">{{ $t("Affected Monitors") }}</h2>
                            {{ $t("affectedMonitorsDescription") }}

                            <div class="my-3">
                                <VueMultiselect
                                    id="affected_monitors"
                                    v-model="affectedMonitors"
                                    :options="affectedMonitorsOptions"
                                    track-by="id"
                                    label="pathName"
                                    :multiple="true"
                                    :close-on-select="false"
                                    :clear-on-select="false"
                                    :preserve-search="true"
                                    :placeholder="$t('Pick Affected Monitors...')"
                                    :preselect-first="false"
                                    :max-height="600"
                                    :taggable="false"
                                ></VueMultiselect>
                            </div>

                            <!-- Status pages to display maintenance info on -->
<!--                            <h2 class="mt-5">{{ $t("Status Pages") }}</h2>-->
<!--                            {{ $t("affectedStatusPages") }}-->

<!--                            <div class="my-3">-->
<!--                                &lt;!&ndash; Show on all pages &ndash;&gt;-->
<!--                                <div class="form-check mb-2">-->
<!--                                    <input-->
<!--                                        id="show-on-all-pages" v-model="showOnAllPages" class="form-check-input"-->
<!--                                        type="checkbox"-->
<!--                                    >-->
<!--                                    <label class="form-check-label" for="show-powered-by">{{-->
<!--                                        $t("All Status Pages")-->
<!--                                    }}</label>-->
<!--                                </div>-->

<!--                                <div v-if="!showOnAllPages">-->
<!--                                    <VueMultiselect-->
<!--                                        id="selected_status_pages"-->
<!--                                        v-model="selectedStatusPages"-->
<!--                                        :options="selectedStatusPagesOptions"-->
<!--                                        track-by="id"-->
<!--                                        label="name"-->
<!--                                        :multiple="true"-->
<!--                                        :close-on-select="false"-->
<!--                                        :clear-on-select="false"-->
<!--                                        :preserve-search="true"-->
<!--                                        :placeholder="$t('Select status pages...')"-->
<!--                                        :preselect-first="false"-->
<!--                                        :max-height="600"-->
<!--                                        :taggable="false"-->
<!--                                    ></VueMultiselect>-->
<!--                                </div>-->
<!--                            </div>-->

                            <h2 class="mt-5">{{ $t("Date and Time") }}</h2>

                            <!-- Strategy -->
<!--                            <div class="my-3">-->
<!--                                <label for="strategy" class="form-label">{{ $t("Strategy") }}</label>-->
<!--                                <select id="strategy" v-model="maintenance.strategy" class="form-select">-->
<!--                                    <option value="manual">{{ $t("strategyManual") }}</option>-->
<!--                                    <option value="single">{{ $t("Single Maintenance Window") }}</option>-->
<!--                                    <option value="cron">{{ $t("cronExpression") }}</option>-->
<!--                                    <option value="recurring-interval">{{ $t("Recurring") }} - {{ $t("recurringInterval") }}</option>-->
<!--                                    <option value="recurring-weekday">{{ $t("Recurring") }} - {{ $t("dayOfWeek") }}</option>-->
<!--                                    <option value="recurring-day-of-month">{{ $t("Recurring") }} - {{ $t("dayOfMonth") }}</option>-->
<!--                                </select>-->
<!--                            </div>-->



                            <template v-if="maintenance.strategy === 'cron'">
                                <!-- Cron -->
                                <div class="my-3">
                                    <label for="cron" class="form-label">
                                        {{ $t("cronExpression") }}
                                    </label>
                                    <p>{{ $t("cronSchedule") }}{{ cronDescription }}</p>
                                    <input id="cron" v-model="maintenance.cron" type="text" class="form-control" required>
                                </div>

                                <div class="my-3">
                                    <!-- Duration -->
                                    <label for="duration" class="form-label">
                                        {{ $t("Duration (Minutes)") }}
                                    </label>
                                    <input id="duration" v-model="maintenance.durationMinutes" type="number" class="form-control" required min="1" step="1">
                                </div>
                            </template>

                            <!-- Recurring - Interval -->
                            <template v-if="maintenance.strategy === 'recurring-interval'">
                                <div class="my-3">
                                    <label for="interval-day" class="form-label">
                                        {{ $t("recurringInterval") }}

                                        <template v-if="maintenance.intervalDay >= 1">
                                            ({{
                                                $tc("recurringIntervalMessage", maintenance.intervalDay, [
                                                    maintenance.intervalDay
                                                ])
                                            }})
                                        </template>
                                    </label>
                                    <input id="interval-day" v-model="maintenance.intervalDay" type="number" class="form-control" required min="1" max="3650" step="1">
                                </div>
                            </template>

                            <!-- Recurring - Weekday -->
                            <template v-if="maintenance.strategy === 'recurring-weekday'">
                                <div class="my-3">
                                    <label for="interval-day" class="form-label">
                                        {{ $t("dayOfWeek") }}
                                    </label>

                                    <!-- Weekday Picker -->
                                    <div class="weekday-picker">
                                        <div v-for="(weekday, index) in weekdays" :key="index">
                                            <label class="form-check-label" :for="weekday.id">{{ $t(weekday.langKey) }}</label>
                                            <div class="form-check-inline"><input :id="weekday.id" v-model="maintenance.weekdays" type="checkbox" :value="weekday.value" class="form-check-input"></div>
                                        </div>
                                    </div>
                                </div>
                            </template>

                            <!-- Recurring - Day of month -->
                            <template v-if="maintenance.strategy === 'recurring-day-of-month'">
                                <div class="my-3">
                                    <label for="interval-day" class="form-label">
                                        {{ $t("dayOfMonth") }}
                                    </label>

                                    <!-- Day Picker -->
                                    <div class="day-picker">
                                        <div v-for="index in 31" :key="index">
                                            <label class="form-check-label" :for="'day' + index">{{ index }}</label>
                                            <div class="form-check-inline">
                                                <input :id="'day' + index" v-model="maintenance.daysOfMonth" type="checkbox" :value="index" class="form-check-input">
                                            </div>
                                        </div>
                                    </div>

                                    <div class="mt-3 mb-2">{{ $t("lastDay") }}</div>

                                    <div v-for="(lastDay, index) in lastDays" :key="index" class="form-check">
                                        <input :id="lastDay.langKey" v-model="maintenance.daysOfMonth" type="checkbox" :value="lastDay.value" class="form-check-input">
                                        <label class="form-check-label" :for="lastDay.langKey">
                                            {{ $t(lastDay.langKey) }}
                                        </label>
                                    </div>
                                </div>
                            </template>

<!--                            <template v-if="maintenance.strategy === 'recurring-interval' || maintenance.strategy === 'recurring-weekday' || maintenance.strategy === 'recurring-day-of-month'">-->
<!--                                &lt;!&ndash; Maintenance Time Window of a Day &ndash;&gt;-->
<!--                                <div class="my-3">-->
<!--                                    <label class="form-label">{{ $t("Maintenance Time Window of a Day") }}</label>-->
<!--                                    <Datepicker-->
<!--                                        v-model="maintenance.timeRange"-->
<!--                                        :dark="$root.isDark"-->
<!--                                        timePicker-->
<!--                                        disableTimeRangeValidation range-->
<!--                                    />-->
<!--                                </div>-->
<!--                            </template>-->

                            <!-- Date Range -->
                            <div class="my-3">

                                <div class="row">
                                    <div class="col">
                                        <div class="mb-2">{{ $t("startDateTime") }}</div>
                                        <input v-model="maintenance.startDate" type="datetime-local" class="form-control" :required="maintenance.strategy === 'single'">
                                    </div>

                                    <div class="col">
                                        <div class="mb-2">{{ $t("endDateTime") }}</div>
                                        <input v-model="maintenance.endDate" type="datetime-local" class="form-control" :required="maintenance.strategy === 'single'">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="fixed-bottom-bar p-3">
                        <button id="monitor-submit-btn" class="btn btn-primary" type="submit" :disabled="processing">{{ $t("Save") }}</button>
                    </div>
                </div>
            </form>
        </div>
    </transition>
</template>

<script>
import { useToast } from "vue-toastification";
import VueMultiselect from "vue-multiselect";
import Datepicker from "@vuepic/vue-datepicker";
import { timezoneList } from "../util-frontend";
import cronstrue from "cronstrue/i18n";

const toast = useToast();

export default {
    components: {
        VueMultiselect,
        Datepicker
    },

    data() {
        return {
            timezoneList: timezoneList(),
            processing: false,
            maintenance: {},
            affectedMonitors: [],
            affectedMonitorsOptions: [],
            showOnAllPages: false,
            selectedStatusPages: [],
            dark: (this.$root.theme === "dark"),
            neverEnd: false,
            lastDays: [
                {
                    langKey: "lastDay1",
                    value: "lastDay1",
                },
            ],
            weekdays: [
                {
                    id: "weekday1",
                    langKey: "weekdayShortMon",
                    value: 1,
                },
                {
                    id: "weekday2",
                    langKey: "weekdayShortTue",
                    value: 2,
                },
                {
                    id: "weekday3",
                    langKey: "weekdayShortWed",
                    value: 3,
                },
                {
                    id: "weekday4",
                    langKey: "weekdayShortThu",
                    value: 4,
                },
                {
                    id: "weekday5",
                    langKey: "weekdayShortFri",
                    value: 5,
                },
                {
                    id: "weekday6",
                    langKey: "weekdayShortSat",
                    value: 6,
                },
                {
                    id: "weekday0",
                    langKey: "weekdayShortSun",
                    value: 0,
                },
            ],
        };
    },

    computed: {

        cronDescription() {
            if (! this.maintenance.cron) {
                return "";
            }

            let locale = "";

            if (this.$root.language) {
                locale = this.$root.language.replace("-", "_");
            }

            // Special handling
            // If locale is also not working in your language, you can map it here
            // https://github.com/bradymholt/cRonstrue/tree/master/src/i18n/locales
            if (locale === "zh_HK") {
                locale = "zh_TW";
            }

            try {
                return cronstrue.toString(this.maintenance.cron, {
                    locale,
                });
            } catch (e) {
                return this.$t("invalidCronExpression", e.message);
            }

        },

        selectedStatusPagesOptions() {
            return Object.values(this.$root.statusPageList).map(statusPage => {
                return {
                    id: statusPage.id,
                    name: statusPage.title
                };
            });
        },

        pageName() {
            return this.$t((this.isAdd) ? "Schedule Maintenance" : "Edit Maintenance");
        },

        isAdd() {
            return this.$route.path === "/add-maintenance";
        },

        isEdit() {
            return this.$route.path.startsWith("/maintenance/edit");
        },

    },
    watch: {
        "$route.fullPath"() {
            this.init();
        },

        neverEnd(value) {
            if (value) {
                this.maintenance.recurringEndDate = "";
            }
        },
    },
    mounted() {
        this.$root.getMonitorList((res) => {
            if (res) {
                Object.values(this.$root.monitorList).sort((m1, m2) => {

                    if (m1.active !== m2.active) {
                        if (m1.active === 0) {
                            return 1;
                        }

                        if (m2.active === 0) {
                            return -1;
                        }
                    }

                    if (m1.weight !== m2.weight) {
                        if (m1.weight > m2.weight) {
                            return -1;
                        }

                        if (m1.weight < m2.weight) {
                            return 1;
                        }
                    }

                    return m1.pathName.localeCompare(m2.pathName);
                }).map(monitor => {
                    this.affectedMonitorsOptions.push({
                        id: monitor.id,
                        pathName: monitor.pathName,
                    });
                });
            }
            this.init();
        });
    },
    methods: {
        /** Initialise page */
        init() {
            this.affectedMonitors = [];
            this.selectedStatusPages = [];

            if (this.isAdd) {
                this.maintenance = {
                    title: "",
                    description: "",
                    active: true,
                    strategy: "single",
                    startDate: new Date().toISOString().slice(0, 16),
                    endDate: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString().slice(0, 16),
                };
            } else if (this.isEdit) {
                this.$root.getSocket().emit("getMaintenance", this.$route.params.id, (res) => {
                    if (res) {
                        this.maintenance = res.maintenance;

                        this.$root.getSocket().emit("getMonitorMaintenance", this.$route.params.id, (res) => {
                            if (res.ok) {
                                Object.values(res.monitors).map(monitor => {
                                    this.affectedMonitors.push(this.affectedMonitorsOptions.find(item => item.id === monitor.id));
                                });
                            } else {
                                toast.error(res.msg);
                            }
                        });

                        // this.$root.getSocket().emit("getMaintenanceStatusPage", this.$route.params.id, (res) => {
                        //     if (res.ok) {
                        //         Object.values(res.statusPages).map(statusPage => {
                        //             this.selectedStatusPages.push({
                        //                 id: statusPage.id,
                        //                 name: statusPage.title
                        //             });
                        //         });
                        //
                        //         this.showOnAllPages = Object.values(res.statusPages).length === this.selectedStatusPagesOptions.length;
                        //     } else {
                        //         toast.error(res.msg);
                        //     }
                        // });
                    } else {
                        toast.error("获取维修事件失败");
                    }
                });
            }
        },

        /** Create new maintenance */
        async submit() {
            this.processing = true;

            if (this.affectedMonitors.length === 0) {
                toast.error(this.$t("atLeastOneMonitor"));
                return this.processing = false;
            }

            if (this.isAdd) {
                this.$root.addMaintenance(this.maintenance, async (res) => {
                    if (res.ok) {
                        await this.addMonitorMaintenance(res.maintenanceID, async () => {
                            // await this.addMaintenanceStatusPage(res.maintenanceID, () => {
                            //     toast.success(res);
                            //     this.processing = false;
                            //     this.$root.getMaintenanceList();
                            //     this.$router.push("/maintenance");
                            // });
                          toast.success("添加维修事件成功");
                          this.processing = false;
                          this.$root.getMaintenanceList();
                          this.$router.push("/maintenance");
                        });
                    } else {
                        toast.error(res.msg);
                        this.processing = false;
                    }

                });
            } else {
                this.$root.getSocket().emit("editMaintenance", this.maintenance, async (res) => {
                    if (res.ok) {
                        await this.addMonitorMaintenance(res.maintenanceID, async () => {
                            // await this.addMaintenanceStatusPage(res.maintenanceID, () => {
                            //     this.processing = false;
                            //     this.$root.toastRes(res);
                            //     this.init();
                            //     this.$router.push("/maintenance");
                            // });
                          this.processing = false;
                          this.$root.toastRes(res);
                          this.init();
                          this.$router.push("/maintenance");
                        });
                    } else {
                        this.processing = false;
                        toast.error(res.msg);
                    }
                });
            }
        },

        /**
         * Add monitor to maintenance
         * @param {number} maintenanceID
         * @param {socketCB} callback
         */
        async addMonitorMaintenance(maintenanceID, callback) {
            await this.$root.addMonitorMaintenance(maintenanceID, this.affectedMonitors, async (res) => {

                if (!res.ok) {
                    toast.error("添加监控修复事件失败");
                } else {
                    this.$root.getMonitorList();
                }

                callback();
            });
        },

        /**
         * Add status page to maintenance
         * @param {number} maintenanceID
         * @param {socketCB} callback
         */
        async addMaintenanceStatusPage(maintenanceID, callback) {
            await this.$root.addMaintenanceStatusPage(maintenanceID, (this.showOnAllPages) ? this.selectedStatusPagesOptions : this.selectedStatusPages, async (res) => {
                if (!res.ok) {
                    toast.error(res.msg);
                } else {
                    this.$root.getMaintenanceList();
                }

                callback();
            });
        },
    },
};
</script>

<style lang="scss" scoped>
textarea {
    min-height: 150px;
}

.dark-calendar::-webkit-calendar-picker-indicator {
    filter: invert(1);
}

.weekday-picker {
    display: flex;
    gap: 10px;

    & > div {
        display: flex;
        flex-direction: column;
        align-items: center;
        width: 40px;

        .form-check-inline {
            margin-right: 0;
        }
    }
}

.day-picker {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;

    & > div {
        display: flex;
        flex-direction: column;
        align-items: center;
        width: 40px;

        .form-check-inline {
            margin-right: 0;
        }
    }
}

</style>
