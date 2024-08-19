<script lang="ts">
import {Vue, Component, Prop, VModel, Watch} from 'vue-property-decorator';
import { getPath } from "@/helper/SVGPaths";
//@ts-ignore
import SvgIcon from '@jamescoyle/vue-icon';

export interface MqttListitem{
  name:string
  id:string
  cat:string
  active:boolean
}

@Component({
  methods: {getPath},
  components:{
    SvgIcon
  }

})
export default class MqttList extends Vue {
  @Prop()items:MqttListitem[]|undefined;
  private listItems:MqttListitem[] = [];

  @Watch('items')
  items_changed(nv:MqttListitem[]){
    nv.forEach((el)=>{
      if(!this.listItems.map(e=>e.id).includes(el.id)){
        this.listItems.push(el);
      }
    });
  }

  get categories(){
    return [...new Set(this.listItems?.map(e=>e.cat))].map(cat=>{ // as  unique Array
      return {
        name:cat,
        active:true,
        childs_shown:true
      }
    })
  }
  get itemsByCategory() {
    return (cat:string)=>{
      return this.listItems?.filter(e=>(e.cat==cat))
  }}
  selected(o:any){
    let items = this.listItems?.find(e=>e.id==o.id)
    if(items){
      items.active = !o.active;
    }
    const enabledTraficLights = this.listItems?.filter(e=>e.active).map(e=>e.cat+'_'+e.name).join(',');
    //query['time'] = Math.random()
    const query = {...this.$route.query};
    query['enabledTraficLights'] =enabledTraficLights;
    this.$router.replace({
                           name: 'datastreams',
                           query: query,
                           replace:true
                         }).catch(err => {

    console.log(err)
  })
  }
}
</script>

<template>
  <div class="mqtttree plane">
    <div class="leaf" v-for="cat in categories"
         :key="cat.name">

      <div class="cat_icon" @click="selected(cat)">
        <svg-icon type="mdi" v-if="getPath(cat.name)" :path="getPath(cat.name)" :size="35" class="svg_icon2"></svg-icon>
        <div class="svg_icon" v-else :class="cat"></div>

      </div>
      <div class="stitle" @click="selected(cat)">
        {{ $t('prop.' + cat.name) }}
      </div>
      <div class="chevron" @click="cat.childs_shown = !cat.childs_shown">
        <i class="mdi mdi-chevron-down" v-if="!cat.childs_shown"></i>
        <i class="mdi mdi-chevron-up" v-else></i>
      </div>
      <div class="childs" v-if="cat.childs_shown">
        <div class="leaf" v-for="child in itemsByCategory(cat.name)" @click="selected(child)" :class="[{'active': child.active},child.name]"
             :key="child.id">
          <div class="cat_icon">
            <svg-icon type="mdi" v-if="getPath(cat.name)" :path="getPath(cat.name)" :size="35" class="svg_icon2"></svg-icon>
            <div class="svg_icon" v-else :class="cat.name"></div>

          </div>

          {{  child.name }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">

.plane {
  /*height: 100%;*/
  z-index: 500;
  background: #3a3a3a;
  border-radius: 2px;
  overflow-y: hidden;
  text-align: left;
  color: #d8d8d8;
  position: relative;
}
.leaf {
  padding-left: 15px;
  display: grid;
  grid-template-columns: 50px 1fr 50px;
  grid-template-rows: 45px;
  align-items: center;
  cursor: pointer;

  .childs{
    grid-column: 1 / 3;
  }
  &.active {
    //background: #1b346fc7;
    background: rgb(27, 52, 111);
    background: linear-gradient(90deg, rgba(27, 52, 111, 0.78) 0%, rgba(27, 52, 111, 0) 100%);
  }
}

.cat_icon {
  width: 40px;
  height: 40px;
  position: relative;

  border-radius: 4px;
  border: 1px solid #999;

  .mdi {
    font-size: 25px;
  }

  .tag {
    position: absolute;
    right: -7px;
    bottom: -6px;
    color: #bbb;
    background: transparent;
  }

  .svg_icon {

    height: 24px;
    width: 24px;
    margin: 7px 2px;

  }
  .cat_icon {
    width: 40px;
    height: 40px;
    position: relative;

    border-radius: 4px;
    border: 1px solid #999;

    .mdi {
      font-size: 25px;
    }

    .tag {
      position: absolute;
      right: -7px;
      bottom: -6px;
      color: #bbb;
      background: transparent;
    }

    .svg_icon {

      height: 24px;
      width: 24px;
      margin: 7px 2px;

    }
  }
}
</style>
