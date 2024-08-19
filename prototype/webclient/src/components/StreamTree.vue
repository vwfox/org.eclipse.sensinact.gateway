<!--
  Copyright (c) 2023 Contributors to the  Eclipse Foundation.

  This program and the accompanying materials are made
  available under the terms of the Eclipse Public License 2.0
  which is available at https://www.eclipse.org/legal/epl-2.0/

  SPDX-License-Identifier: EPL-2.0

  Contributors: Markus Hochstein
-->


<template>
  <div class="plane tree">
    <b-loading :active="loading" :can-cancel="false" :is-full-page="false"></b-loading>

      <div class="tree">
        <div class="leaf" v-for="leaf in treeData"  :class="[{'active': leaf.active},leaf.text]"
              :key="leaf.key">
          <div class="cat_icon" @click="selected(leaf)">
            <svg-icon type="mdi" v-if="getPath([leaf.text])" :path="getPath([leaf.text])" :size="35" class="svg_icon2"></svg-icon>
            <div class="svg_icon" v-else :class="[leaf.text]"></div>
            <b-tag rounded type="is-primary">{{ leaf._data.length }}</b-tag>
          </div>
          <div class="stitle" @click="selected(leaf)">
          {{ $t('prop.' + leaf.text) }}
          </div>
          <div class="chevron" @click="leaf.childs_shown = !leaf.childs_shown">
            <i class="mdi mdi-chevron-down" v-if="!leaf.childs_shown"></i>
            <i class="mdi mdi-chevron-up" v-else></i>
          </div>
          <div class="childs" v-if="leaf.childs_shown">
              <div class="leaf" v-for="child in leaf.children" @click="selected(child)" :class="[{'active': child.active},child.text]"
                   :key="child.key">
                <div class="cat_icon">
                  <svg-icon type="mdi" v-if="getPath([leaf.text])" :path="getPath([leaf.text])" :size="35" class="svg_icon2"></svg-icon>
                  <div class="svg_icon" v-else :class="[leaf.text]"></div>

                </div>

                {{  child.text }}
            </div>
          </div>
        </div>
      </div>


  </div>
</template>

<script lang="ts">

import {Component, Vue, Watch} from "vue-property-decorator";
import {
  Configuration,
  Datastream,
  Datastreams,
  DatastreamsApi,
  Location, Locations,
  LocationsApi, Observation, Observations,
  Thing,
  Things,
  ThingsApi
} from "../../openapi/client";
import ThingsC from "@/components/Thing.vue";
//@ts-ignore
import VTreeview from "v-treeview"
import {getBaseUrl} from "@/config/base";
import groupByName, {groupByCategory, groupByCategoryAndThing} from "@/helper/DatastreamGroups";
//@ts-ignore
import SvgIcon from '@jamescoyle/vue-icon';

import { getPath } from "@/helper/SVGPaths";

@Component({
  components: {
    VTreeview,
    SvgIcon
  }
})
export default class StreamTreeC extends Vue {


   private loading = false;


  private treeData: any = [];

  private selectedNodesKeys: any = {};
  private oldEnabledCategories = '';

  @Watch('$route.query') query_changed_out(new_query_params:any){
    this.query_changed(new_query_params)
  }


  query_changed(new_query_params: any,force=false) {
    if ( (force || this.oldEnabledCategories!=new_query_params.enabledCategories)) {
      let array_of_key_to_select = new_query_params.enabledCategories.split(',');
      this.treeData.forEach((node: any) => {
        if (array_of_key_to_select.includes(node.key)) {
          this.selectedNodesKeys[node.key] = node._data;
          node.active = true;
        }
      })
      let emit: any = [];
      for (let key in this.selectedNodesKeys) {
        emit = emit.concat(this.selectedNodesKeys[key])
      }
      this.oldEnabledCategories = new_query_params.enabledCategories;
      this.$emit('selection', emit)
    }
  }

  mounted() {
    //this.getDatascreamsTree()
    //this.query_changed(this.$route.query)

  }

  getPath(id:string){
    return getPath(id[0])
  }

  async selected(node: any) {

    if(node.children){
      if (node.active) {
        for (let child of node.children) {
          delete this.selectedNodesKeys[child.key];
          child.active = false;
        }
        node.active = false;
      }
      else {
          for(let child of node.children){
            this.selectedNodesKeys[child.key] = child._data;
            child.active = true;
          }
        node.active = true;
      }

    }else{
      if (node.active) {
        delete this.selectedNodesKeys[node.key];
        node.active = false;
      } else {
        this.selectedNodesKeys[node.key] = node._data;
        node.active = true;
      }
    }


    /*let emit:any = [];
    for(let key in this.selectedNodesKeys){
      console.log(this.selectedNodesKeys[key])
      emit = emit.concat(this.selectedNodesKeys[key])
    }
    console.log(emit)
    console.log(Object.keys(this.selectedNodesKeys).join(','))*/

    let query:any = {};
    try{
      //query = this.$route.query;
      /*if(this.$route.query.enabledCategories){
        query['enabledCategories'] = this.$route.query.enabledCategories
      }*/
      if(this.$route.query.coord){
        query['coord'] = this.$route.query.coord
      }
      if(this.$route.query.zoom){
        query['zoom'] = this.$route.query.zoom
      }
      if(this.$route.query.enabledTraficLights){
        query['enabledTraficLights'] = this.$route.query.enabledTraficLights
      }

      query['enabledCategories'] = Object.keys(this.selectedNodesKeys).join(',');
      //query['time'] = Math.random()
    }
    catch (e){
      console.log(e)
    }


    this.$router.replace({
      name: 'datastreams',
      query: {...query}
    }).catch(err => {
      //console.log(err)
    })
    /*this.$router.replace({
      name: 'datastreams',
      query: {enabledCategories: Object.keys(this.selectedNodesKeys).join(',')}
    })*/
    //this.$emit('selection', emit)

  }


  async getDatascreamsTree(datastreams: Array<Datastream>,things?:Array<Thing>) {



    let ret: unknown[] = [];
    //let groups = groupByCategory(datastreams)
    let groups = groupByCategoryAndThing(datastreams,things)
    for (const [key, value] of Object.entries(groups)) {

      const v = value as Array<Datastream>;
      let node = {
        id: Math.random() * 100000,
        text: key,
        type: "FMM_DATASTREAM",
        children: v.map((entry:Datastream)=>{
          const spl:string[] = ((entry["@iot.id"]??'~')as string).split('~');
          return {
          id: Math.random() * 100000,
          text: spl[0]+'~'+spl[1]??'',
          type: "FMM_THING",
          _data: entry,
          key:entry["@iot.id"],
          active: false
        }}),
        _data: value,
        key: key,
        active: false,
        childs_shown:true
      }
      ret.push(node);
    }
    this.treeData = ret;
    this.query_changed(this.$route.query,true);
  }
  async getDatascreamsTreeThings(things: Array<Thing>|undefined) {
    this.loading = true;

    this.loading = false;
    let ret: unknown[] = [];
    let groups = groupByCategory(things)
    for (const [key, value] of Object.entries(groups)) {


      let node = {
        id: Math.random() * 100000,
        text: key,
        type: "FMM_DATASTREAM",
        children: [],
        _data: value,
        key: key,
        active: false
      }
      ret.push(node);
    }
    this.treeData = ret;
    this.query_changed(this.$route.query,true);
  }
  get isactive() {
    return Object.keys(this.selectedNodesKeys)
  }

}


</script>

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

.cap {
  font-style: italic;
  font-weight: bold;

}

.pad {
  padding: 0 10px;
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
}
</style>
<style lang="scss">
@import "./../scss/general";

ul .tree-node {
  white-space: nowrap;
}

ul .tree-node :hover::before {
  background: rgba(135, 147, 161, 0.27) !important;
}

ul .tree-node input[type=radio]:checked + label:before {
  background: #4099ff3d !important;
}


</style>
