<template>
    <div class="shadow-box mb-3" :style="boxStyle">
        <div class="list-header">
            <div class="header-top">
                <button class="btn btn-outline-normal ms-2" :class="{ 'active': selectMode }" type="button" @click="selectMode = !selectMode">
                    {{ $t("Select") }}
                </button>

                <div class="placeholder"></div>
                <div class="search-wrapper">
                    <a v-if="searchText == ''" class="search-icon">
                        <font-awesome-icon icon="search" />
                    </a>
                    <a v-if="searchText != ''" class="search-icon" @click="clearSearchText">
                        <font-awesome-icon icon="times" />
                    </a>
                    <form>
                        <input
                            v-model="searchText"
                            class="form-control search-input"
                            :placeholder="$t('Search...')"
                            :aria-label="$t('Search monitored sites')"
                            autocomplete="off"
                        />
                    </form>
                </div>
            </div>
            <div class="header-filter">
                <MonitorListFilter :filterState="filterState" @update-filter="updateFilter" />
            </div>

            <!-- Selection Controls -->
            <div v-if="selectMode" class="selection-controls px-2 pt-2">
                <input
                    v-model="selectAll"
                    class="form-check-input select-input"
                    type="checkbox"
                />

                <button class="btn-outline-normal" @click="pauseDialog"><font-awesome-icon icon="pause" size="sm" /> {{ $t("Pause") }}</button>
                <button class="btn-outline-normal" @click="resumeSelected"><font-awesome-icon icon="play" size="sm" /> {{ $t("Resume") }}</button>

                <span v-if="selectedMonitorCount > 0">
                    {{ $t("selectedMonitorCount", [ selectedMonitorCount ]) }}
                </span>
            </div>
        </div>
        <div ref="monitorList" class="monitor-list" :class="{ scrollbar: scrollbar }" :style="monitorListStyle">
            <div v-if="Object.keys($root.monitorList).length === 0" class="text-center mt-3">
                {{ $t("No Monitors, please") }} <router-link to="/add">{{ $t("add one") }}</router-link>
            </div>

            <MonitorListItem
                v-for="(item, index) in sortedMonitorList"
                :key="index"
                :monitor="item"
                :showPathName="filtersActive"
                :isSelectMode="selectMode"
                :isSelected="isSelected"
                :select="select"
                :deselect="deselect"
            />
        </div>
    </div>

    <Confirm ref="confirmPause" :yes-text="$t('Yes')" :no-text="$t('No')" @yes="pauseSelected">
        {{ $t("pauseMonitorMsg") }}
    </Confirm>
</template>

<script>
import Confirm from "../components/Confirm.vue";
import MonitorListItem from "../components/MonitorListItem.vue";
import MonitorListFilter from "./MonitorListFilter.vue";
import { getMonitorRelativeURL } from "../util.ts";

export default {
    components: {
        Confirm,
        MonitorListItem,
        MonitorListFilter,
    },
    props: {
        /** Should the scrollbar be shown */
        scrollbar: {
            type: Boolean,
        },
    },
    data() {
        return {
            searchText: "",
            selectMode: false,
            selectAll: false,
            disableSelectAllWatcher: false,
            selectedMonitors: {},
            windowTop: 0,
            filterState: {
                status: null,
                active: null,
                tags: null,
            }
        };
    },
    computed: {
        /**
         * Improve the sticky appearance of the list by increasing its
         * height as user scrolls down.
         * Not used on mobile.
         */
        boxStyle() {
            if (window.innerWidth > 550) {
                return {
                    height: `calc(100vh - 160px + ${this.windowTop}px)`,
                };
            } else {
                return {
                    height: "calc(100vh - 160px)",
                };
            }

        },

        /**
         * Returns a sorted list of monitors based on the applied filters and search text.
         *
         * @return {Array} The sorted list of monitors.
         */
        sortedMonitorList() {
          try {
            // 获取所有监控项
            let result = Object.values(this.$root.monitorList);

            // 如果有搜索或过滤，才应用过滤逻辑
            const hasFilters =
                this.searchText !== "" ||
                (this.filterState.status != null && this.filterState.status.length > 0) ||
                (this.filterState.active != null && this.filterState.active.length > 0) ||
                (this.filterState.tags != null && this.filterState.tags.length > 0);

            // 如果没有过滤器激活，则返回所有监控项(可选择只显示父级监控项)
            if (!hasFilters) {
              // 如果要只显示父级监控项，使用下面一行
              // return result.filter(monitor => monitor && monitor.parent === null);

              // 如果要显示所有监控项，使用下面一行
              return result.filter(monitor => monitor !== null);
            }

            // 应用过滤逻辑，与现有代码相同，但添加了安全检查
            result = result.filter(monitor => {
              if (!monitor) return false;

              // 搜索文本匹配
              let searchTextMatch = true;
              if (this.searchText !== "") {
                const loweredSearchText = this.searchText.toLowerCase();
                searchTextMatch =
                    (monitor.name ? monitor.name.toLowerCase().includes(loweredSearchText) : false) ||
                    (monitor.tags && Array.isArray(monitor.tags) && monitor.tags.some(tag =>
                        (tag && tag.name ? tag.name.toLowerCase().includes(loweredSearchText) : false) ||
                        (tag && tag.value ? tag.value.toLowerCase().includes(loweredSearchText) : false)
                    ));
              }

              // 状态过滤
              let statusMatch = true;
              if (this.filterState.status != null && this.filterState.status.length > 0) {
                if (this.$root.lastHeartbeatList &&
                    monitor.id in this.$root.lastHeartbeatList &&
                    this.$root.lastHeartbeatList[monitor.id]) {
                  monitor.status = this.$root.lastHeartbeatList[monitor.id].status;
                }
                statusMatch = monitor.status != null && this.filterState.status.includes(monitor.status);
              }

              // 活动状态过滤
              let activeMatch = true;
              if (this.filterState.active != null && this.filterState.active.length > 0) {
                activeMatch = monitor.active != null && this.filterState.active.includes(monitor.active);
              }

              // 标签过滤
              let tagsMatch = true;
              if (this.filterState.tags != null && this.filterState.tags.length > 0) {
                if (monitor.tags && Array.isArray(monitor.tags)) {
                  const monitorTagIds = monitor.tags
                      .filter(tag => tag != null)
                      .map(tag => tag.tag_id)
                      .filter(id => id != null);
                  tagsMatch = monitorTagIds.length > 0 &&
                      monitorTagIds.some(id => this.filterState.tags.includes(id));
                } else {
                  tagsMatch = false;
                }
              }

              // 移除子项隐藏逻辑，使所有项目默认显示
              /*
              let showChild = true;
              if (this.filterState.status == null && this.filterState.active == null &&
                  this.filterState.tags == null && this.searchText === "") {
                  if (monitor.parent !== null) {
                      showChild = false;
                  }
              }
              */
              const showChild = true; // 始终显示所有项目，包括子项

              return searchTextMatch && statusMatch && activeMatch && tagsMatch && showChild;
            });

            // 排序逻辑保持不变，但添加安全检查
            result.sort((m1, m2) => {
              if (!m1 || !m2) return 0;

              if (m1.active !== m2.active) {
                if (m1.active === false) {
                  return 1;
                }
                if (m2.active === false) {
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

              return m1.name && m2.name ? m1.name.localeCompare(m2.name) : 0;
            });

            return result;
          } catch (error) {
            console.error("Error in sortedMonitorList:", error);
            return []; // 出错时返回空数组
          }
        },

        isDarkTheme() {
            return document.body.classList.contains("dark");
        },

        monitorListStyle() {
            let listHeaderHeight = 107;

            if (this.selectMode) {
                listHeaderHeight += 42;
            }

            return {
                "height": `calc(100% - ${listHeaderHeight}px)`
            };
        },

        selectedMonitorCount() {
            return Object.keys(this.selectedMonitors).length;
        },

        /**
         * Determines if any filters are active.
         *
         * @return {boolean} True if any filter is active, false otherwise.
         */
        filtersActive() {
            return this.filterState.status != null || this.filterState.active != null || this.filterState.tags != null || this.searchText !== "";
        }
    },
    watch: {
        searchText() {
            for (let monitor of this.sortedMonitorList) {
                if (!this.selectedMonitors[monitor.id]) {
                    if (this.selectAll) {
                        this.disableSelectAllWatcher = true;
                        this.selectAll = false;
                    }
                    break;
                }
            }
        },
        selectAll() {
            if (!this.disableSelectAllWatcher) {
                this.selectedMonitors = {};

                if (this.selectAll) {
                    this.sortedMonitorList.forEach((item) => {
                        this.selectedMonitors[item.id] = true;
                    });
                }
            } else {
                this.disableSelectAllWatcher = false;
            }
        },
        selectMode() {
            if (!this.selectMode) {
                this.selectAll = false;
                this.selectedMonitors = {};
            }
        },
    },
    mounted() {
        window.addEventListener("scroll", this.onScroll);
    },
    beforeUnmount() {
        window.removeEventListener("scroll", this.onScroll);
    },
    methods: {
        /** Handle user scroll */
        onScroll() {
            if (window.top.scrollY <= 133) {
                this.windowTop = window.top.scrollY;
            } else {
                this.windowTop = 133;
            }
        },
        /**
         * Get URL of monitor
         * @param {number} id ID of monitor
         * @returns {string} Relative URL of monitor
         */
        monitorURL(id) {
            return getMonitorRelativeURL(id);
        },
        /** Clear the search bar */
        clearSearchText() {
            this.searchText = "";
        },
        /**
         * Update the MonitorList Filter
         * @param {object} newFilter Object with new filter
         */
        updateFilter(newFilter) {
            this.filterState = newFilter;
        },
        /**
         * Deselect a monitor
         * @param {number} id ID of monitor
         */
        deselect(id) {
            delete this.selectedMonitors[id];
        },
        /**
         * Select a monitor
         * @param {number} id ID of monitor
         */
        select(id) {
            this.selectedMonitors[id] = true;
        },
        /**
         * Determine if monitor is selected
         * @param {number} id ID of monitor
         * @returns {bool}
         */
        isSelected(id) {
            return id in this.selectedMonitors;
        },
        /** Disable select mode and reset selection */
        cancelSelectMode() {
            this.selectMode = false;
            this.selectedMonitors = {};
        },
        /** Show dialog to confirm pause */
        pauseDialog() {
            this.$refs.confirmPause.show();
        },
        /** Pause each selected monitor */
        pauseSelected() {
            Object.keys(this.selectedMonitors)
                .filter(id => this.$root.monitorList[id].active)
                .forEach(id => this.$root.getSocket().emit("pauseMonitor", id));

            this.cancelSelectMode();
        },
        /** Resume each selected monitor */
        resumeSelected() {
            Object.keys(this.selectedMonitors)
                .filter(id => !this.$root.monitorList[id].active)
                .forEach(id => this.$root.getSocket().emit("resumeMonitor", id));

            this.cancelSelectMode();
        },
    },
};
</script>

<style lang="scss" scoped>
@import "../assets/vars.scss";

.shadow-box {
    height: calc(100vh - 150px);
    position: sticky;
    top: 10px;
}

.small-padding {
    padding-left: 5px !important;
    padding-right: 5px !important;
}

.list-header {
    border-bottom: 1px solid #dee2e6;
    border-radius: 10px 10px 0 0;
    margin: -10px;
    margin-bottom: 10px;
    padding: 10px;

    .dark & {
        background-color: $dark-header-bg;
        border-bottom: 0;
    }
}

.header-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.header-filter {
    display: flex;
    align-items: center;
}

@media (max-width: 770px) {
    .list-header {
        margin: -20px;
        margin-bottom: 10px;
        padding: 5px;
    }
}

.search-wrapper {
    display: flex;
    align-items: center;
}

.search-icon {
    padding: 10px;
    color: #c0c0c0;

    // Clear filter button (X)
    svg[data-icon="times"] {
        cursor: pointer;
        transition: all ease-in-out 0.1s;

        &:hover {
            opacity: 0.5;
        }
    }
}

.search-input {
    max-width: 15em;
}

.monitor-item {
    width: 100%;
}

.tags {
    margin-top: 4px;
    padding-left: 67px;
    display: flex;
    flex-wrap: wrap;
    gap: 0;
}

.bottom-style {
    padding-left: 67px;
    margin-top: 5px;
}

.selection-controls {
    margin-top: 5px;
    display: flex;
    align-items: center;
    gap: 10px;
}

</style>
