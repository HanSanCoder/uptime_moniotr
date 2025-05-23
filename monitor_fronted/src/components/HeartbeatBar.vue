<template>
  <div ref="wrap" class="wrap" :style="wrapStyle">
    <div class="hp-bar-big" :style="barStyle">
      <div
          v-for="(beat, index) in shortBeatList"
          :key="index"
          class="beat"
          :class="{ 'empty': (beat === 0), 'down': (beat.status === 0), 'pending': (beat.status === 2), 'maintenance': (beat.status === 3) }"
          :style="beatStyle"
          :title="getBeatTitle(beat)"
      />
    </div>
    <div
        v-if="!$root.isMobile && size !== 'small' && beatList.length > 4 && $root.styleElapsedTime !== 'none'"
        class="d-flex justify-content-between align-items-center word" :style="timeStyle"
    >
      <div>{{ timeSinceFirstBeat }} ago</div>
      <div v-if="$root.styleElapsedTime === 'with-line'" class="connecting-line"></div>
      <div>{{ timeSinceLastBeat }}</div>
    </div>
  </div>
</template>

<script>
import dayjs from "dayjs";

export default {
  props: {
    /** Size of the heartbeat bar */
    size: {
      type: String,
      default: "big",
    },
    /** ID of the monitor */
    monitorId: {
      type: Number,
      required: true,
    },
    /** Array of the monitors heartbeats */
    heartbeatList: {
      type: Array,
      default: null,
    }
  },
  data() {
    return {
      beatWidth: 10,
      beatHeight: 30,
      hoverScale: 1.5,
      beatMargin: 4,
      move: false,
      maxBeat: -1,
    };
  },
  computed: {

    /**
     * If heartbeatList is null, get it from $root.heartbeatList
     */
    beatList() {
      if (this.heartbeatList === null) {
        return this.$root.heartbeatList[this.monitorId];
      } else {
        return this.heartbeatList;
      }
    },

    /**
     * Calculates the amount of beats of padding needed to fill the length of shortBeatList.
     *
     * @return {number} The amount of beats of padding needed to fill the length of shortBeatList.
     */
    numPadding() {
      if (!this.beatList) {
        return 0;
      }
      let num = this.beatList.length - this.maxBeat;

      if (this.move) {
        num = num - 1;
      }

      if (num > 0) {
        return 0;
      }

      return -1 * num;
    },

    shortBeatList() {
      if (!this.beatList) {
        return [];
      }

      // Simply take the first maxBeat elements
      const end = Math.min(this.maxBeat, this.beatList.length);
      
      // Return the first portion of beatList
      return this.beatList.slice(0, end);
    },

    wrapStyle() {
      let topBottom = (((this.beatHeight * this.hoverScale) - this.beatHeight) / 2);
      let leftRight = (((this.beatWidth * this.hoverScale) - this.beatWidth) / 2);

      return {
        padding: `${topBottom}px ${leftRight}px`,
        width: "100%",
      };
    },

    barStyle() {
      if (this.move && this.shortBeatList.length > this.maxBeat) {
        let width = -(this.beatWidth + this.beatMargin * 2);

        return {
          transition: "all ease-in-out 0.25s",
          transform: `translateX(${width}px)`,
        };

      }
      return {
        transform: "translateX(0)",
      };

    },

    beatStyle() {
      return {
        width: this.beatWidth + "px",
        height: this.beatHeight + "px",
        margin: this.beatMargin + "px",
        "--hover-scale": this.hoverScale,
      };
    },

    /**
     * Returns the style object for positioning the time element.
     * @return {Object} The style object containing the CSS properties for positioning the time element.
     */
    timeStyle() {
      return {
        "margin-left": this.numPadding * (this.beatWidth + this.beatMargin * 2) + "px",
      };
    },

    /**
     * Calculates the time elapsed since the first valid beat.
     *
     * @return {string} The time elapsed in minutes or hours.
     */
    timeSinceFirstBeat() {
      const firstValidBeat = this.shortBeatList.at(this.numPadding);
      // 使用我们的 parseTime 方法替代 dayjs.utc
      const minutes = dayjs().diff(this.parseTime(firstValidBeat?.time), "minutes");
      if (minutes > 60) {
        return (minutes / 60).toFixed(0) + "h";
      } else {
        return minutes + "m";
      }
    },

    /**
     * Calculates the elapsed time since the last valid beat was registered.
     *
     * @return {string} The elapsed time in a minutes, hours or "now".
     */
    timeSinceLastBeat() {
      const lastValidBeat = this.shortBeatList.at(-1);
      // 使用我们的 parseTime 方法替代 dayjs.utc
      const seconds = dayjs().diff(this.parseTime(lastValidBeat?.time), "seconds");

      let tolerance = 60 * 2; // default for when monitorList not available
      if (this.$root.monitorList[this.monitorId] != null) {
        tolerance = this.$root.monitorList[this.monitorId].interval * 2;
      }

      if (seconds < tolerance) {
        return "now";
      } else if (seconds < 60 * 60) {
        return (seconds / 60).toFixed(0) + "m ago";
      } else {
        return (seconds / 60 / 60).toFixed(0) + "h ago";
      }
    }
  },
  watch: {
    beatList: {
      handler(val, oldVal) {
        this.move = true;

        setTimeout(() => {
          this.move = false;
        }, 300);
      },
      deep: true,
    },
  },
  unmounted() {
    window.removeEventListener("resize", this.resize);
  },
  beforeMount() {
    if (this.heartbeatList === null) {
      if (!(this.monitorId in this.$root.heartbeatList)) {
        this.$root.heartbeatList[this.monitorId] = [];
      }
    }
  },

  mounted() {
    if (this.size !== "big") {
      this.beatWidth = 5;
      this.beatHeight = 16;
      this.beatMargin = 2;
    }

    // Suddenly, have an idea how to handle it universally.
    // If the pixel * ratio != Integer, then it causes render issue, round it to solve it!!
    const actualWidth = this.beatWidth * window.devicePixelRatio;
    const actualMargin = this.beatMargin * window.devicePixelRatio;

    if (!Number.isInteger(actualWidth)) {
      this.beatWidth = Math.round(actualWidth) / window.devicePixelRatio;
    }

    if (!Number.isInteger(actualMargin)) {
      this.beatMargin = Math.round(actualMargin) / window.devicePixelRatio;
    }

    window.addEventListener("resize", this.resize);
    this.resize();
  },
  methods: {
    /** Resize the heartbeat bar */
    resize() {
      if (this.$refs.wrap) {
        this.maxBeat = Math.floor(this.$refs.wrap.clientWidth / (this.beatWidth + this.beatMargin * 2));
      }
    },

    /**
     * Get the title of the beat.
     * Used as the hover tooltip on the heartbeat bar.
     * @param {Object} beat Beat to get title from
     * @returns {string}
     */
    getBeatTitle(beat) {
      if (!beat || beat === 0) {
        return "";
      }
      // 使用我们的 parseTime 方法格式化时间
      return `${this.parseTime(beat.time).format('YYYY-MM-DD HH:mm:ss')}` + ((beat.msg) ? ` - ${beat.msg}` : "");
    },

    /**
     * 解析各种格式的时间
     * @param {string|number} time - 时间值
     * @returns {Object} dayjs对象
     */
    parseTime(time) {
      if (!time) {
        return dayjs();
      }
      if (typeof time === 'number') {
        return dayjs(time);
      } else if (typeof time === 'string') {
        // 处理 LocalDateTime 格式
        return dayjs(time);
      }
      return dayjs();
    },

  },
};
</script>

<style lang="scss" scoped>
@import "../assets/vars.scss";

.wrap {
  overflow: hidden;
  width: 100%;
  white-space: nowrap;
}

.hp-bar-big {
  .beat {
    display: inline-block;
    background-color: $primary;
    border-radius: $border-radius;

    &.empty {
      background-color: aliceblue;
    }

    &.down {
      background-color: $danger;
    }

    &.pending {
      background-color: $warning;
    }

    &.maintenance {
      background-color: $maintenance;
    }

    &:not(.empty):hover {
      transition: all ease-in-out 0.15s;
      opacity: 0.8;
      transform: scale(var(--hover-scale));
    }
  }
}

.dark {
  .hp-bar-big .beat.empty {
    background-color: #848484;
  }
}

.word {
  color: #aaa;
  font-size: 12px;
}

.connecting-line {
  flex-grow: 1;
  height: 1px;
  background-color: #ededed;
  margin-left: 10px;
  margin-right: 10px;
  margin-top: 2px;

  .dark & {
    background-color: #333;
  }
}
</style>
