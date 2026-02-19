<script setup>
import AppHeader from './components/layout/AppHeader.vue'
</script>

<template>
  <AppHeader />
  <main>
    <router-view />
  </main>
</template>

<style scoped>
main {
  min-height: calc(100vh - 56px);
}
</style>
