<script setup>
import { RouterView } from 'vue-router'
import { onMounted } from 'vue';

onMounted(() => {
  const imagesToPreload = [
    new URL('@/assets/background-1.png', import.meta.url).href,
    new URL('@/assets/background-2.png', import.meta.url).href,
  ];

  imagesToPreload.forEach((src) => {
    const img = new Image();
    img.src = src;
  });
});
</script>

<template>
  <RouterView />
</template>

<style scoped>
/* 你可以在这里添加全局样式 */
</style>