<template>
  <router-view />
</template>

<script setup lang="ts">
// App root component
</script>

<style>
/* Global styles */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* Element Plus overrides */
.el-menu--horizontal {
  border-bottom: none !important;
}
</style>
