<template>
  <div>
    <div :style="depthMargin">
      <!-- Checkbox -->
      <div v-if="isSelectMode" class="select-input-wrapper">
        <input
            class="form-check-input select-input"
            type="checkbox"
            :aria-label="$t('Check/Uncheck')"
            :checked="isSelected(monitor.id)"
            @click.stop="toggleSelection"
        />
      </div>

      <router-link :to="monitorURL(monitor.id)" class="item" :class="{ 'disabled': ! monitor.active }">
        <div class="row">
          <div class="col-9 col-md-8 small-padding"
               :class="{ 'monitor-item': $root.userHeartbeatBar == 'bottom' || $root.userHeartbeatBar == 'none' }">
            <div class="info">
              <Uptime :monitor="monitor" type="24" :pill="true"/>
              <span v-if="hasChildren" class="collapse-padding" @click.prevent="changeCollapsed">
                                <font-awesome-icon icon="chevron-down" class="animated"
                                                   :class="{ collapsed: isCollapsed}"/>
                            </span>
              {{ monitorName }}
            </div>
            <div v-if="monitor.tags.length > 0" class="tags">
              <Tag v-for="tag in monitor.tags" :key="tag" :item="tag" :size="'sm'"/>
            </div>
          </div>
          <div v-show="$root.userHeartbeatBar == 'normal'" :key="$root.userHeartbeatBar" class="col-3 col-md-4">
            <HeartbeatBar ref="heartbeatBar" size="small" :monitor-id="monitor.id"/>
          </div>
        </div>

        <div v-if="$root.userHeartbeatBar == 'bottom'" class="row">
          <div class="col-12 bottom-style">
            <HeartbeatBar ref="heartbeatBar" size="small" :monitor-id="monitor.id"/>
          </div>
        </div>
      </router-link>
    </div>

    <transition name="slide-fade-up">
      <div v-if="!isCollapsed" class="childs">
        <MonitorListItem
            v-for="(item, index) in sortedChildMonitorList"
            :key="index" :monitor="item"
            :showPathName="showPathName"
            :isSelectMode="isSelectMode"
            :isSelected="isSelected"
            :select="select"
            :deselect="deselect"
            :depth="depth + 1"
        />
      </div>
    </transition>
  </div>
</template>

<script>
import HeartbeatBar from "../components/HeartbeatBar.vue";
import Tag from "../components/Tag.vue";
import Uptime from "../components/Uptime.vue";
import {getMonitorRelativeURL} from "../util.ts";

export default {
  name: "MonitorListItem",
  components: {
    Uptime,
    HeartbeatBar,
    Tag,
  },
  props: {
    /** Monitor this represents */
    monitor: {
      type: Object,
      default: null,
    },
    /** Should the monitor name show it's parent */
    showPathName: {
      type: Boolean,
      default: false,
    },
    /** If the user is in select mode */
    isSelectMode: {
      type: Boolean,
      default: false,
    },
    /** How many ancestors are above this monitor */
    depth: {
      type: Number,
      default: 0,
    },
    /** Callback to determine if monitor is selected */
    isSelected: {
      type: Function,
      default: () => {
      }
    },
    /** Callback fired when monitor is selected */
    select: {
      type: Function,
      default: () => {
      }
    },
    /** Callback fired when monitor is deselected */
    deselect: {
      type: Function,
      default: () => {
      }
    },
  },
  data() {
    return {
      isCollapsed: false,
    };
  },
  computed: {
    sortedChildMonitorList() {
      let result = Object.values(this.$root.monitorList);

      result = result.filter(childMonitor => childMonitor.parent === this.monitor.id);

      result.sort((m1, m2) => {

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

        return m1.name.localeCompare(m2.name);
      });

      return result;
    },
    hasChildren() {
      return this.sortedChildMonitorList.length > 0;
    },
    depthMargin() {
      return {
        marginLeft: `${31 * this.depth}px`,
      };
    },
    monitorName() {
      return this.monitor.name;
    },
  },
  watch: {
    isSelectMode() {
      // TODO: Resize the heartbeat bar, but too slow
      // this.$refs.heartbeatBar.resize();
    }
  },
  beforeMount() {
    // 默认设置为展开状态
    this.isCollapsed = false;

    // 处理路由参数 - 检查是否需要根据路由展开
    try {
      if (this.$route && this.$route.params && this.$route.params.id) {
        const routeId = parseInt(this.$route.params.id);

        // 如果childrenIDs存在且包含当前路由ID，确保展开
        if (this.monitor.childrenIDs && Array.isArray(this.monitor.childrenIDs) &&
            this.monitor.childrenIDs.includes(routeId)) {
          this.isCollapsed = false;
          return;
        }

        // 根据计算属性检查子监控项
        if (this.sortedChildMonitorList && this.sortedChildMonitorList.some(child => child.id === routeId)) {
          this.isCollapsed = false;
          return;
        }
      }

      // 检查本地存储中的折叠状态
      const storage = window.localStorage.getItem("monitorCollapsed");
      if (storage) {
        const storageObject = JSON.parse(storage);
        if (storageObject && typeof storageObject === 'object') {
          const storeKey = `monitor_${this.monitor.id}`;
          // 只有明确设置为true时才折叠
          if (storageObject[storeKey] === true) {
            this.isCollapsed = true;
          }
        }
      }
    } catch (error) {
      console.error("Error in MonitorListItem.beforeMount:", error);
      // 出错时默认展开，确保用户可以看到内容
      this.isCollapsed = false;
    }
  },
  methods: {
    /**
     * Changes the collapsed value of the current monitor and saves it to local storage
     */
    changeCollapsed() {
      this.isCollapsed = !this.isCollapsed;

      // Save collapsed value into local storage
      let storage = window.localStorage.getItem("monitorCollapsed");
      let storageObject = {};
      if (storage !== null) {
        storageObject = JSON.parse(storage);
      }
      storageObject[`monitor_${this.monitor.id}`] = this.isCollapsed;

      window.localStorage.setItem("monitorCollapsed", JSON.stringify(storageObject));
    },
    /**
     * Get URL of monitor
     * @param {number} id ID of monitor
     * @returns {string} Relative URL of monitor
     */
    monitorURL(id) {
      return getMonitorRelativeURL(id);
    },
    /**
     * Toggle selection of monitor
     */
    toggleSelection() {
      if (this.isSelected(this.monitor.id)) {
        this.deselect(this.monitor.id);
      } else {
        this.select(this.monitor.id);
      }
    },
  },
};
</script>

<style lang="scss" scoped>
@import "../assets/vars.scss";

.small-padding {
  padding-left: 5px !important;
  padding-right: 5px !important;
}

.collapse-padding {
  padding-left: 8px !important;
  padding-right: 2px !important;
}

// .monitor-item {
//     width: 100%;
// }

.tags {
  margin-top: 4px;
  padding-left: 67px;
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.collapsed {
  transform: rotate(-90deg);
}

.animated {
  transition: all 0.2s $easing-in;
}

.select-input-wrapper {
  float: left;
  margin-top: 15px;
  margin-left: 3px;
  margin-right: 10px;
  padding-left: 4px;
  position: relative;
  z-index: 15;
}

</style>
