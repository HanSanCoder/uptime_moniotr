<template>
    <transition name="slide-fade" appear>
        <div v-if="monitor">
            <router-link v-if="group !== ''" :to="monitorURL(monitor.parent)"> {{ group }}</router-link>
            <h1> {{ monitor.name }}</h1>
            <p v-if="monitor.description">{{ monitor.description }}</p>
            <div class="tags">
                <Tag v-for="tag in monitor.tags" :key="tag.id" :item="tag" :size="'sm'" />
            </div>
            <p class="url">
                <a v-if="monitor.type === 'http' || monitor.type === 'keyword' || monitor.type === 'json-query' || monitor.type === 'mp-health' " :href="monitor.url" target="_blank" rel="noopener noreferrer">{{ filterPassword(monitor.url) }}</a>
                <span v-if="monitor.type === 'port'">TCP Port {{ monitor.hostname }}:{{ monitor.port }}</span>
                <span v-if="monitor.type === 'ping'">Ping: {{ monitor.hostname }}</span>
                <span v-if="monitor.type === 'keyword'">
                    <br>
                    <span>{{ $t("Keyword") }}: </span>
                    <span class="keyword">{{ monitor.keyword }}</span>
                    <span v-if="monitor.invertKeyword" alt="Inverted keyword" class="keyword-inverted"> ↧</span>
                </span>
                <span v-if="monitor.type === 'json-query'">
                    <br>
                    <span>{{ $t("Json Query") }}:</span> <span class="keyword">{{ monitor.jsonPath }}</span>
                    <br>
                    <span>{{ $t("Expected Value") }}:</span> <span class="keyword">{{ monitor.expectedValue }}</span>
                </span>
                <span v-if="monitor.type === 'dns'">[{{ monitor.dns_resolve_type }}] {{ monitor.hostname }}
                    <br>
                    <span>{{ $t("Last Result") }}:</span> <span class="keyword">{{ monitor.dns_last_result }}</span>
                </span>
                <span v-if="monitor.type === 'docker'">Docker container: {{ monitor.docker_container }}</span>
                <span v-if="monitor.type === 'gamedig'">Gamedig - {{ monitor.game }}: {{ monitor.hostname }}:{{ monitor.port }}</span>
                <span v-if="monitor.type === 'grpc-keyword'">gRPC - {{ filterPassword(monitor.grpcUrl) }}
                    <br>
                    <span>{{ $t("Keyword") }}:</span> <span class="keyword">{{ monitor.keyword }}</span>
                </span>
                <span v-if="monitor.type === 'mongodb'">{{ filterPassword(monitor.databaseConnectionString) }}</span>
                <span v-if="monitor.type === 'mqtt'">MQTT: {{ monitor.hostname }}:{{ monitor.port }}/{{ monitor.mqttTopic }}</span>
                <span v-if="monitor.type === 'mysql'">{{ filterPassword(monitor.databaseConnectionString) }}</span>
                <span v-if="monitor.type === 'postgres'">{{ filterPassword(monitor.databaseConnectionString) }}</span>
                <span v-if="monitor.type === 'push'">Push: <a :href="pushURL" target="_blank" rel="noopener noreferrer">{{ pushURL }}</a></span>
                <span v-if="monitor.type === 'radius'">Radius: {{ filterPassword(monitor.hostname) }}</span>
                <span v-if="monitor.type === 'redis'">{{ filterPassword(monitor.databaseConnectionString) }}</span>
                <span v-if="monitor.type === 'sqlserver'">SQL Server: {{ filterPassword(monitor.databaseConnectionString) }}</span>
                <span v-if="monitor.type === 'steam'">Steam Game Server: {{ monitor.hostname }}:{{ monitor.port }}</span>
            </p>

            <div class="functions">
                <div class="btn-group" role="group">
                    <button v-if="monitor.active" class="btn btn-normal" @click="pauseDialog">
                        <font-awesome-icon icon="pause" /> {{ $t("Pause") }}
                    </button>
                    <button v-if="! monitor.active" class="btn btn-primary" :disabled="monitor.forceInactive" @click="resumeMonitor">
                        <font-awesome-icon icon="play" /> {{ $t("Resume") }}
                    </button>
                    <router-link :to=" '/edit/' + monitor.id " class="btn btn-normal">
                        <font-awesome-icon icon="edit" /> {{ $t("Edit") }}
                    </router-link>
<!--                    <router-link :to=" '/clone/' + monitor.id " class="btn btn-normal">-->
<!--                        <font-awesome-icon icon="clone" /> {{ $t("Clone") }}-->
<!--                    </router-link>-->
                    <button class="btn btn-danger" @click="deleteDialog">
                        <font-awesome-icon icon="trash" /> {{ $t("Delete") }}
                    </button>
                </div>
            </div>

            <div class="shadow-box">
                <div class="row">
                    <div class="col-md-8">
                        <HeartbeatBar :monitor-id="monitor.id" />
                        <span class="word">{{ $t("checkEverySecond", [ monitor.interval ]) }}</span>
                    </div>
                    <div class="col-md-4 text-center">
                        <span class="badge rounded-pill" :class=" 'bg-' + status.color " style="font-size: 30px;">{{ status.text }}</span>
                    </div>
                </div>
            </div>

            <!-- Stats -->
            <div class="shadow-box big-padding text-center stats">
                <div class="row">
                    <div v-if="monitor.type !== 'group'" class="col-12 col-sm col row d-flex align-items-center d-sm-block">
                        <h4 class="col-4 col-sm-12">{{ pingTitle() }}</h4>
                        <p class="col-4 col-sm-12 mb-0 mb-sm-2">({{ $t("Current") }})</p>
                        <span class="col-4 col-sm-12 num">
                            <a href="#" @click.prevent="showPingChartBox = !showPingChartBox">
                                <CountUp :value="ping" />
                            </a>
                        </span>
                    </div>
                    <div v-if="monitor.type !== 'group'" class="col-12 col-sm col row d-flex align-items-center d-sm-block">
                        <h4 class="col-4 col-sm-12">{{ pingTitle(true) }}</h4>
                        <p class="col-4 col-sm-12 mb-0 mb-sm-2">(24{{ $t("-hour") }})</p>
                        <span class="col-4 col-sm-12 num">
                            <CountUp :value="avgPing" />
                        </span>
                    </div>
                    <div class="col-12 col-sm col row d-flex align-items-center d-sm-block">
                        <h4 class="col-4 col-sm-12">{{ $t("Uptime") }}</h4>
                        <p class="col-4 col-sm-12 mb-0 mb-sm-2">(24{{ $t("-hour") }})</p>
                        <span class="col-4 col-sm-12 num">
                            <Uptime :monitor="monitor" type="24" />
                        </span>
                    </div>
                    <div class="col-12 col-sm col row d-flex align-items-center d-sm-block">
                        <h4 class="col-4 col-sm-12">{{ $t("Uptime") }}</h4>
                        <p class="col-4 col-sm-12 mb-0 mb-sm-2">(30{{ $t("-day") }})</p>
                        <span class="col-4 col-sm-12 num">
                            <Uptime :monitor="monitor" type="720" />
                        </span>
                    </div>

                    <div v-if="tlsInfo" class="col-12 col-sm col row d-flex align-items-center d-sm-block">
                        <h4 class="col-4 col-sm-12">{{ $t("Cert Exp.") }}</h4>
                        <p class="col-4 col-sm-12 mb-0 mb-sm-2">(<Datetime :value="tlsInfo.certInfo.validTo" date-only />)</p>
                        <span class="col-4 col-sm-12 num">
                            <a href="#" @click.prevent="toggleCertInfoBox = !toggleCertInfoBox">{{ tlsInfo.certInfo.daysRemaining }} {{ $tc("day", tlsInfo.certInfo.daysRemaining) }}</a>
                        </span>
                    </div>
                </div>
            </div>

            <!-- Cert Info Box -->
            <transition name="slide-fade" appear>
                <div v-if="showCertInfoBox" class="shadow-box big-padding text-center">
                    <div class="row">
                        <div class="col">
                            <certificate-info :certInfo="tlsInfo.certInfo" :valid="tlsInfo.valid" />
                        </div>
                    </div>
                </div>
            </transition>

            <!-- Ping Chart -->
            <div v-if="showPingChartBox" class="shadow-box big-padding text-center ping-chart-wrapper">
                <div class="row">
                    <div class="col">
                        <PingChart :monitor-id="monitor.id" />
                    </div>
                </div>
            </div>

            <!-- Screenshot -->
            <div v-if="monitor.type === 'real-browser'" class="shadow-box">
                <div class="row">
                    <div class="col-md-6">
                        <img :src="screenshotURL" alt style="width: 100%;">
                    </div>
                </div>
            </div>

            <div class="shadow-box table-shadow-box">
                <div class="dropdown dropdown-clear-data">
                    <button class="btn btn-sm btn-outline-danger dropdown-toggle" type="button" data-bs-toggle="dropdown">
                        <font-awesome-icon icon="trash" /> {{ $t("Clear Data") }}
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li>
                            <button type="button" class="dropdown-item" @click="clearEventsDialog">
                                {{ $t("Events") }}
                            </button>
                        </li>
                        <li>
                            <button type="button" class="dropdown-item" @click="clearHeartbeatsDialog">
                                {{ $t("Heartbeats") }}
                            </button>
                        </li>
                    </ul>
                </div>
                <table class="table table-borderless table-hover">
                    <thead>
                        <tr>
                            <th>{{ $t("Status") }}</th>
                            <th>{{ $t("DateTime") }}</th>
                            <th>{{ $t("Message") }}</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(beat, index) in displayedRecords" :key="index" style="padding: 10px;">
                            <td><Status :status="beat.status" /></td>
<!--                            <td :class="{ 'border-0':! beat.msg}"><Datetime :value="beat.time" /></td>-->
                            <td :class="{ 'border-0':! beat.msg}">{{ beat.time }}</td>
                            <td class="border-0">{{ beat.msg }}</td>
                        </tr>

                        <tr v-if="importantHeartBeatList.length === 0">
                            <td colspan="3">
                                {{ $t("No important events") }}
                            </td>
                        </tr>
                    </tbody>
                </table>

                <div class="d-flex justify-content-center kuma_pagination">
                    <pagination
                        v-model="page"
                        :records="importantHeartBeatList.length"
                        :per-page="perPage"
                        :options="paginationConfig"
                    />
                </div>
            </div>

            <Confirm ref="confirmPause" :yes-text="$t('Yes')" :no-text="$t('No')" @yes="pauseMonitor">
                {{ $t("pauseMonitorMsg") }}
            </Confirm>

            <Confirm ref="confirmDelete" btn-style="btn-danger" :yes-text="$t('Yes')" :no-text="$t('No')" @yes="deleteMonitor">
                {{ $t("deleteMonitorMsg") }}
            </Confirm>

            <Confirm ref="confirmClearEvents" btn-style="btn-danger" :yes-text="$t('Yes')" :no-text="$t('No')" @yes="clearEvents">
                {{ $t("clearEventsMsg") }}
            </Confirm>

            <Confirm ref="confirmClearHeartbeats" btn-style="btn-danger" :yes-text="$t('Yes')" :no-text="$t('No')" @yes="clearHeartbeats">
                {{ $t("clearHeartbeatsMsg") }}
            </Confirm>
        </div>
    </transition>
</template>

<script>
import { defineAsyncComponent } from "vue";
import { useToast } from "vue-toastification";
const toast = useToast();
import Confirm from "../components/Confirm.vue";
import HeartbeatBar from "../components/HeartbeatBar.vue";
import Status from "../components/Status.vue";
import Datetime from "../components/Datetime.vue";
import CountUp from "../components/CountUp.vue";
import Uptime from "../components/Uptime.vue";
import Pagination from "v-pagination-3";
const PingChart = defineAsyncComponent(() => import("../components/PingChart.vue"));
import Tag from "../components/Tag.vue";
import CertificateInfo from "../components/CertificateInfo.vue";
import { getMonitorRelativeURL } from "../util.ts";
import { URL } from "whatwg-url";
import { getResBaseURL } from "../util-frontend";

export default {
    components: {
        Uptime,
        CountUp,
        Datetime,
        HeartbeatBar,
        Confirm,
        Status,
        Pagination,
        PingChart,
        Tag,
        CertificateInfo,
    },
    data() {
        return {
            page: 1,
            perPage: 25,
            heartBeatList: [],
            toggleCertInfoBox: false,
            showPingChartBox: true,
            paginationConfig: {
                hideCount: true,
                chunksNavigation: "scroll",
            },
            cacheTime: Date.now(),
        };
    },
    computed: {
        monitor() {
            let id = this.$route.params.id;
            return this.$root.monitorList[id];
        },

        lastHeartBeat() {
            // Also trigger screenshot refresh here
            // eslint-disable-next-line vue/no-side-effects-in-computed-properties
            this.cacheTime = Date.now();

            if (this.monitor.id in this.$root.lastHeartbeatList && this.$root.lastHeartbeatList[this.monitor.id]) {
                return this.$root.lastHeartbeatList[this.monitor.id];
            }

            return {
                status: -1,
            };
        },

        ping() {
            if (this.lastHeartBeat.ping || this.lastHeartBeat.ping === 0) {
                return this.lastHeartBeat.ping;
            }

            return this.$t("notAvailableShort");
        },

        avgPing() {
            if (this.$root.avgPingList[this.monitor.id] || this.$root.avgPingList[this.monitor.id] === 0) {
                return this.$root.avgPingList[this.monitor.id];
            }

            return this.$t("notAvailableShort");
        },

        importantHeartBeatList() {
            if (this.$root.importantHeartbeatList[this.monitor.id]) {
                // eslint-disable-next-line vue/no-side-effects-in-computed-properties
                this.heartBeatList = this.$root.importantHeartbeatList[this.monitor.id];
                return this.$root.importantHeartbeatList[this.monitor.id];
            }

            return [];
        },

        status() {
            if (this.$root.statusList[this.monitor.id]) {
                return this.$root.statusList[this.monitor.id];
            }

            return { };
        },

        tlsInfo() {
            // Add: this.$root.tlsInfoList[this.monitor.id].certInfo
            // Fix: TypeError: Cannot read properties of undefined (reading 'validTo')
            // Reason: TLS Info object format is changed in 1.8.0, if for some reason, it cannot connect to the site after update to 1.8.0, the object is still in the old format.
            if (this.$root.tlsInfoList[this.monitor.id] && this.$root.tlsInfoList[this.monitor.id].certInfo) {
                return this.$root.tlsInfoList[this.monitor.id];
            }

            return null;
        },

        showCertInfoBox() {
            return this.tlsInfo != null && this.toggleCertInfoBox;
        },

        displayedRecords() {
            const startIndex = this.perPage * (this.page - 1);
            const endIndex = startIndex + this.perPage;
            return this.heartBeatList.slice(startIndex, endIndex);
        },

        group() {
            if (!this.monitor.pathName.includes("/")) {
                return "";
            }
            return this.monitor.pathName.substr(0, this.monitor.pathName.lastIndexOf("/"));
        },

        pushURL() {
            return this.$root.baseURL + "/api/push/" + this.monitor.pushToken + "?status=up&msg=OK&ping=";
        },

        screenshotURL() {
            return getResBaseURL() + this.monitor.screenshot + "?time=" + this.cacheTime;
        }
    },
    mounted() {

    },
    methods: {
        getResBaseURL,
        /** Request a test notification be sent for this monitor */
        testNotification() {
            this.$root.getSocket().emit("testNotification", this.monitor.id);
            toast.success("Test notification is requested.");
        },

        /** Show dialog to confirm pause */
        pauseDialog() {
            this.$refs.confirmPause.show();
        },

        /** Resume this monitor */
        resumeMonitor() {
            this.$root.getSocket().emit("resumeMonitor", this.monitor.id, (res) => {
                this.$root.toastRes(res);
            });
        },

        /** Request that this monitor is paused */
        pauseMonitor() {
            this.$root.getSocket().emit("pauseMonitor", this.monitor.id, (res) => {
                this.$root.toastRes(res);
            });
        },

        /** Show dialog to confirm deletion */
        deleteDialog() {
            this.$refs.confirmDelete.show();
        },

        /** Show dialog to confirm clearing events */
        clearEventsDialog() {
            this.$refs.confirmClearEvents.show();
        },

        /** Show dialog to confirm clearing heartbeats */
        clearHeartbeatsDialog() {
            this.$refs.confirmClearHeartbeats.show();
        },

        /** Request that this monitor is deleted */
        deleteMonitor() {
            this.$root.deleteMonitor(this.monitor.id, (res) => {
                if (res.ok) {
                    toast.success(res.msg);
                    this.$router.push("/dashboard");
                } else {
                    toast.error(res.msg);
                }
            });
        },

        /** Request that this monitors events are cleared */
        clearEvents() {
            this.$root.clearEvents(this.monitor.id, (res) => {
                if (! res.ok) {
                    toast.error("清除事件数据失败");
                }
            });
        },

        /** Request that this monitors heartbeats are cleared */
        clearHeartbeats() {
            this.$root.clearHeartbeats(this.monitor.id, (res) => {
                if (! res.ok) {
                    toast.error("清除心跳数据失败");
                }
            });
        },

        /**
         * Return the correct title for the ping stat
         * @param {boolean} [average=false] Is the statistic an average?
         * @returns {string} Title formated dependant on monitor type
         */
        pingTitle(average = false) {
            let translationPrefix = "";
            if (average) {
                translationPrefix = "Avg. ";
            }

            if (this.monitor.type === "http" || this.monitor.type === "keyword" || this.monitor.type === "json-query") {
                return this.$t(translationPrefix + "Response");
            }

            return this.$t(translationPrefix + "Ping");
        },

        /**
         * Get URL of monitor
         * @param {number} id ID of monitor
         * @returns {string} Relative URL of monitor
         */
        monitorURL(id) {
            return getMonitorRelativeURL(id);
        },

        /** Filter and hide password in URL for display */
        filterPassword(urlString) {
            try {
                let parsedUrl = new URL(urlString);
                if (parsedUrl.password !== "") {
                    parsedUrl.password = "******";
                }
                return parsedUrl.toString();
            } catch (e) {
                // Handle SQL Server
                return urlString.replaceAll(/Password=(.+);/ig, "Password=******;");
            }
        }
    },
};
</script>

<style lang="scss" scoped>
@import "../assets/vars.scss";

@media (max-width: 767px) {
    .badge {
        margin-top: 14px;
    }
}

@media (max-width: 550px) {
    .functions {
        text-align: center;
    }

    .ping-chart-wrapper {
        padding: 10px !important;
    }

    .dropdown-clear-data {
        margin-bottom: 10px;
    }
}

@media (max-width: 400px) {
    .btn {
        display: inline-flex;
        flex-direction: column;
        align-items: center;
        padding-top: 10px;
        font-size: 0.9em;
    }

    a.btn {
        padding-left: 25px;
        padding-right: 25px;
    }

    .dropdown-clear-data {
        button {
            display: block;
            padding-top: 4px;
        }
    }
}

.url {
    color: $primary;
    margin-bottom: 20px;
    font-weight: bold;

    a {
        color: $primary;
    }
}

.shadow-box {
    padding: 20px;
    margin-top: 25px;
}

.word {
    color: #aaa;
    font-size: 14px;
}

table {
    font-size: 14px;

    tr {
        transition: all ease-in-out 0.2ms;
    }
}

.stats p {
    font-size: 13px;
    color: #aaa;
}

.stats {
    padding: 10px;

    .col {
        margin: 20px 0;
    }
}

@media (max-width: 550px) {
    .stats {
        .col {
            margin: 10px 0 !important;
        }

        h4 {
            font-size: 1.1rem;
        }
    }
}

.keyword {
    color: black;
}

.dropdown-clear-data {
    float: right;

    ul {
        width: 100%;
        min-width: unset;
        padding-left: 0;
    }
}

.dark {
    .keyword {
        color: $dark-font-color;
    }

    .keyword-inverted {
        color: $dark-font-color;
    }

    .dropdown-clear-data {
        ul {
            background-color: $dark-bg;
            border-color: $dark-bg2;
            border-width: 2px;

            li button {
                color: $dark-font-color;
            }

            li button:hover {
                background-color: $dark-bg2;
            }
        }
    }
}

.tags {
    margin-bottom: 0.5rem;
}

.tags > div:first-child {
    margin-left: 0 !important;
}

</style>
