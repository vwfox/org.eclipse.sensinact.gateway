<template>
  <div class="datapoint">
    {{data}} {{unit}}
  </div>
</template>
<script lang="ts">

import {Component, Prop, Vue, Watch} from "vue-property-decorator";

@Component
export default class Datapoint extends Vue{
  private data = ''
  @Prop({default:()=>''}) readonly unit:any
  @Prop() readonly id:any
  mounted(){
    /*setInterval(()=>{
      console.log(this.obs)
    },1000)*/
  }
  @Watch('$sstore.obs.state.obs') handler(data:any){
    console.log('obs_change')
    if(this.$sstore.obs.state.obs && this.$sstore.obs.state.obs[this.id] && this.$sstore.obs.state.obs[this.id].result){
      this.data = this.$sstore.obs.state.obs[this.id].result;

    }else{
      this.data = '';
    }
  }

}

</script>
<style lang="scss" scoped>
.datapoint{
  white-space: nowrap;
}
</style>
