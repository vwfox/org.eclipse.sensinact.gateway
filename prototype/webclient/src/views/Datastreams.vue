<!--
  Copyright (c) 2023 Contributors to the  Eclipse Foundation.

  This program and the accompanying materials are made
  available under the terms of the Eclipse Public License 2.0
  which is available at https://www.eclipse.org/legal/epl-2.0/

  SPDX-License-Identifier: EPL-2.0

  Contributors: Markus Hochstein

-->

<template>


  <div class="grid" id="app">
    <div class="url colspan2 titlebar">
      <div class="logo">
        <div class="white triangle"></div>
        <div class="small logo smart_city_project" id="logo"></div>

      </div>
      <!--<b-field>
        <b-input v-model="baseurl"></b-input>
        <b-button  outlined @click="connect()">Connect</b-button>
      </b-field>-->
    </div>
    <div class="map_holder">
      <l-map id="map" :zoom="zoom" :center="center" @click="deselect" @update:center="updateCenter" @update:zoom="updateZoom" :max-zoom="21">
        <l-tile-layer :url="url" :attribution="attribution" :options="{maxNativeZoom:19,
        maxZoom:25}"></l-tile-layer>

        <template  v-for="(viewport,key) in viewports">
        <l-geo-json  :geojson="viewport" :options="{'pointToLayer':pointToLayer}" v-bind:key="key"
                     :options-style="styleP"
                     v-if=" $route.query.enabledTraficLights && $route.query.enabledTraficLights.includes('ViewPort_'+key.toString())"
                     ></l-geo-json>
        </template>

        <template  v-for="(geoJsonTragicLight,key) in observationsGeoJsonMqtt">
          <l-geo-json  :geojson="geoJsonTragicLight" :options="{'pointToLayer':pointToLayer}" v-bind:key="key"
                       :options-style="styleP"
                       v-if=" $route.query.enabledTraficLights && $route.query.enabledTraficLights.includes('TrafiCam_'+key.toString())"></l-geo-json>
        </template>



       <l-geo-json v-for="features in geojson"  :geojson="features.location" :key="features['@iot.id']+'_area'"  :options-style="style(features['@iot.id'])"></l-geo-json>


        <v-marker-cluster :options="{spiderfyDistanceMultiplier:3.2,animate:true,animateAddingMarkers:true,zoomToBoundsOnClick:false,disableClusteringAtZoom:18}"
                          ref="clusterRef2" v-if="centerPoints && centerPoints.length>0">

          <template v-for="point in centerPoints" >
          <custom-marker @click.native="(ev)=>{ev.stopImmediatePropagation();markerWasClicked(point)}" :key="point['@iot.id']+'markr'"
                         :marker="ret(point.location.geometry.coordinates)"
                         :lat-lng="ret(point.location.geometry.coordinates)" v-if="point.location.geometry">


           <!-- <div class='marker-pin' :class="{'selected':point===selected}">-->
             <!-- <div class="round">-->
            <div class='marker-pin' :class="{'selected':point===selected}"  v-if="point['@iot.id'].split('~').reverse()[0]!=='color'">
              <div class="round" v-if="point['@iot.id'].split('~').reverse()[0]!='color'">
                <svg-icon type="mdi"
                          :size="24"
                          v-if="getPath(point['@iot.id'].split('~').reverse()[0])"
                          :path="getPath(point['@iot.id'].split('~').reverse()[0])"
                          class="marker_svg"></svg-icon>
                <div class="svg_icon dark"
                     :class="point['@iot.id'].split('~').reverse()[0]"
                     v-else-if="point['@iot.id'].split('~').reverse()[0]">
                </div>

              </div>
            </div>
                <div class="marker-value" @click="(ev)=>{ev.stopImmediatePropagation();markerWasClicked(point)}" :class="point['@iot.id'].split('~').reverse()[0]">


                  <Datapoint :id="point['@iot.id']" :unit="''" :is-bool="point['@iot.id'].split('~').reverse()[0]=='conflict'"></Datapoint>


                </div>
              <!--</div>-->
          <!--  </div>-->







          </custom-marker>
          </template>
        </v-marker-cluster>

      </l-map>


    </div>
    <div class="sidebar_holder absolute">

      <perfect-scrollbar>
        <StreamTree ref="streamTree" @selection="select"></StreamTree>
        <MqttList :items="mqtt_items" ></MqttList>
      </perfect-scrollbar>


    </div>
    <div class="propertie_holder absolute" v-if="selected!==null">
      <b-button class="absbtn" type="is-text" rounded size="is-small"
                icon-right="close" @click="deselect">
      </b-button>
      <PropertiesC :data="selectedData"></PropertiesC>
    </div>

  </div>
</template>

<script lang="ts">
import {Component, Vue, Watch} from "vue-property-decorator";
import {LIcon, LMap, LMarker, LTileLayer, LWMSTileLayer,LGeoJson} from "vue2-leaflet";
import {
  LocationsApi,
  Location,
  Locations,
  Configuration,
  Datastream,
  ThingsApi,
  DatastreamsApi,
  ObservationsApi, Observations, Datastreams, Things, Thing
} from "../../openapi/client";
import PropertiesC from "@/components/PropertiesView/Properties.vue";
import {BASE_PATH} from "../../openapi/client/base";
import {getBaseUrl, setBaseUrl} from "@/config/base";
import StreamTree from "@/components/StreamTree.vue";
import {AxiosResponse} from "axios";
//@ts-ignore

import Vue2LeafletMarkercluster from "vue2-leaflet-markercluster/Vue2LeafletMarkercluster.vue";
//@ts-ignore
import CustomMarker from 'vue-leaflet-custom-marker';
import Datapoint from "@/components/Datapoint.vue";
import { getPath } from "@/helper/SVGPaths";
//@ts-ignore
import SvgIcon from '@jamescoyle/vue-icon';

import L from "leaflet";

import * as turf from '@turf/turf'
import conf from '@/config/mqtt.json';
import MqttList from "@/components/MqttList.vue";

export interface LocationsPlus {
  dsid: String | undefined
}

@Component({
  components: {
    Datapoint,
    StreamTree,
    PropertiesC,
    LMap,
    LTileLayer,
    LMarker,
    LIcon,
    LGeoJson,
    LWMSTileLayer,
    'v-marker-cluster': Vue2LeafletMarkercluster,
    CustomMarker,
    SvgIcon,MqttList

  }
})
export default class DatastreamsV extends Vue {
  //private url = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';

  private url = 'https://a.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png'
  //private url = 'https://map.jena.de/wms/kartenportal';
  private attribution =
    '&copy; <a href="https://www.stadiamaps.com/" target="_blank">Stadia Maps</a> &copy; <a href="https://openmaptiles.org/" target="_blank">OpenMapTiles</a> &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';
  private zoom: number = 14;

  private center = [50.93115286, 11.60392726];
  private markerLatLng = [55.8382551745062, -4.20119980206699]
  private locations: Array<Location> | undefined = [];
  private selected: Location | null = null;
  private obs: any = {};
  private treeData: unknown = null;
  //private baseurl:string = 'https://sensors.bgs.ac.uk/FROST-Server';
  private baseurl: string = getBaseUrl();
  private datastreams: Array<Datastream> | null = null;
  private things: Array<Thing> | undefined = [];
  private datastreamsbyID: { [key: string]: Datastream } = {};
  private timer: any = null;
  private selectedData: any = null;
  private mqtt: Worker | null = null;
  campoints = null;
  observationsGeoJsonMqtt = null;
  viewports = null;
  observationsGeoJsonMqttConfig = null;
  private mqttmenu = {};
  private mqtt_items = {};
  async mounted() {

    this.mqtt = new Worker(new URL("@/worker/mqtt.ts", import.meta.url));
    this.mqtt.postMessage('connect');
    this.mqtt.postMessage('subscribe');
    this.mqtt.onmessage = (evt)=>{

      let types:any = {};
      let MqttItems:any = [];
      /*Object.values(evt.data.features).forEach((feature:any)=>{
        if(feature.properties && feature.properties.thing){
          types[feature.properties.thing]=1;
        }
      })*/
      Object.keys(evt.data.features).forEach((r:any)=>{
        MqttItems.push({name:r,id:'TrafiCam_'+r,cat:'TrafiCam',active:false})
      })
      Object.keys(evt.data.configs).forEach((r:any)=>{
        MqttItems.push({name:r,id:'ViewPort_'+r,cat:'ViewPort',active:false})
      })
      this.mqtt_items = MqttItems;
      for( const [key, value] of Object.entries(evt.data.features)){
        evt.data.features[key].features = Object.values(evt.data.features[key].features);
      }
      for( const [key, value] of Object.entries(evt.data.configs)){
        evt.data.configs[key].features = Object.values(evt.data.configs[key].features);
      }
      this.viewports = evt.data.configs;
      this.observationsGeoJsonMqtt = evt.data.features;

      /*this.observationsGeoJsonMqtt =  {
        type: "FeatureCollection",
        features: Object.values(evt.data.features)
      }as any*/
      /*this.observationsGeoJsonMqttConfig =  {
        type: "FeatureCollection",
        features: Object.values(evt.data.configs)
      }as any
      this.campoints=  {
        type: "FeatureCollection",
        features: Object.values(evt.data.cams)
      }as any
      this.mqttmenu = {cams:evt.data.cams,configs:evt.data.configs};
      */

      //console.log(this.mqttmenu)

    }
    //this.SetobservationsGeoJsonMqtt();
    let query = this.$route.query;
    if (this.$route.query.zoom) {
      try {
        let zoom = parseInt(this.$route.query.zoom as string);
        if (zoom > 1 && zoom < 19) {
          this.zoom = zoom;
        }
      } catch (e) {
        //parse Error
      }

    }
    if (this.$route.query.coord) {
      try {
        let splitArr = (this.$route.query.coord as string).split(',');
        let lat = parseFloat(splitArr[0]);
        let lng = parseFloat(splitArr[1]);
        if (lat > -90 && lat < 90 && lng > -180 && lng < 180) {
          this.center = [lat, lng];
        }
      } catch (e) {
        //parse Error
      }

    }
    await this.load();
  }

  async load() {
    console.log('load');
    ///@ts-ignore
    this.datastreams = (await new DatastreamsApi(new Configuration({basePath: getBaseUrl()}))
      .v11DatastreamsGet()).data.value as Array<Datastream>;

    //@ts-ignore
    //this.datastreams = dataStreamMock.value as Array<Datastream>;

    this.things = (await new ThingsApi(new Configuration({basePath: getBaseUrl()}))
      .v11ThingsGet()).data.value;

    // (this.$refs.streamTree as StreamTree).getDatascreamsTreeThings(this.things);

    if (!this.datastreams) {
      this.datastreams=[];
    }
    this.datastreams = this.datastreams.map(datastream=>{
      if(datastream.observedArea && ['Polygon'].includes((datastream.observedArea as any).type)){
        let geometry = datastream.observedArea;
        (datastream.observedArea as any) = {
          type:"Feature",
          properties:{},
          geometry:geometry
        }
      }
      return datastream;
    }) as Datastream[];

      (this.$refs.streamTree as StreamTree).getDatascreamsTree(this.datastreams,this.things);
      for (let datastream of this.datastreams) {
        //@ts-ignore
        this.datastreamsbyID[datastream["@iot.id"]] = datastream;
      }
    }



  deselect() {
    this.selected = null;
    //@ts-ignore
    this.selectedData = null;
  }

  res(arr: any) {
    return [arr[1], arr[0]]
  }

  ret(arr: any) {
    return {
      lat: arr[1],
      lng: arr[0]
    }
  }

  rev(arr: any) {
    return {
      lat: arr[0],
      lng: arr[1]
    }
  }

  connect() {
    setBaseUrl(this.baseurl)
    this.load();

  }

  checkFeatureCollectionRaw(point: any) {
    if (!point.type) return false;
    if (point.type !== 'FeatureCollection') return false;

    return true;
  }

  checkFeatureCollection(point: any) {
    if (!point.location) return false;
    if (!point.location.type) return false;
    if (! ['FeatureCollection','Point','LineString','Polygon','MultiPoint','MultiLineString','MultiPolygon','Feature'].includes(point.location.type)) return false;

    return true;
  }

  checkPoint(point: any) {
    if (!point.location) return false;
    if (!point.location.latitude) return false;
    if (!point.location.longitude) return false;


    return true;
  }


  get geojson() {

    if (!this.locations || this.locations.length==0) return [];
    const map = this.locations.filter(this.checkFeatureCollection);
    console.log(map)
    return map;
  }

  get observationsGeoJson() {
    try {
      let ret = Object.values(this.$sstore.obs.state.obs).filter((e: any) => {
        return this.checkFeatureCollectionRaw(e.result);
      });
      if (!ret) return [];
      return ret;
    } catch (r) {
      console.log(r);
      return []
    }
  }

  get points() {
    if (!this.locations) return [];
    let map = this.locations.filter(this.checkPoint);
    return map;
  }

  get centerPoints() {

      return this.geojson.map(e => {
        let f = {...e};

        try {
          if(e.location){
            if((e.location as any).type=='FeatureCollection'){
              (e.location! as any).properties={} as any;
            }

            //@ts-ignore
            //if(e.location.features)
            //f.location = turf.center(e.location.features[0]);
            f.location = turf.center(e.location);
            console.log(f)
          }
        } catch (err) {
          console.log(err);

        }
        return f;
      })

  }


  markerWasClicked(point: Location & LocationsPlus) {
    this.selected = point;
    //@ts-ignore
    this.selectedData = {data: this.datastreamsbyID[point['@iot.id']], type: 'FMM_DATASTREAM'}
  }


  async select(model: Datastream[]) {
    console.log('model')
    console.log(model)
    this.locations = [];
    let proms: Promise<any>[] = [];
    model.forEach((datastream: Datastream) => {
      if (datastream && datastream["@iot.id"]) {
        if (datastream.observedArea) {
          //@ts-ignore

          let loctype = {...datastream} as any;
          loctype.observedArea['properties'] = {"@iot.id": datastream["@iot.id"]}
          loctype["@iot.id"] = datastream["@iot.id"];
          loctype['location'] = {
            type: "FeatureCollection",
            features: [loctype.observedArea],
          };
          this.locations?.push(loctype);
        } else {


          //@ts-ignore
          proms.push(new Promise(async (res, rej) => {
            try {
              //@ts-ignore
              let result = await new ThingsApi(new Configuration({basePath: getBaseUrl()})).v11ThingsEntityIdLocationsGet((datastream["@iot.id"].toString().split('~')[0]));
              if (result.data && result.data.value && result.data.value[0]) {
                //@ts-ignore
                (result.data.value[0] as LocationsPlus)['@iot.id'] = datastream["@iot.id"];
              }
              res(result);
            } catch (e) {
              rej(e)
            }
          }));
        }
      }
    })
    let thingsLoaction: AxiosResponse<Locations & LocationsPlus>[] = await Promise.all(proms);

    this.locations = this.locations.concat(
      thingsLoaction.map((e: AxiosResponse<Locations & LocationsPlus>) => {
      return (e.data.value![0])
    })
    );
    console.log('setPoints');

    //this.locations.push({"@iot.id": "karl"} as Location)

    try{
      this.$sstore.obs.setPoints(this.locations);
      this.$sstore.obs.getDataForPoints();
      if (this.locations.length > 0) {
        this.$sstore.obs.settimer();
      } else {
        this.$sstore.obs.clearTimer();
      }
    }catch (e){
      console.log(e);
    }

    if(this.$refs.clusterRef2){
      //@ts-ignore
      this.$refs.clusterRef2!.mapObject.refreshClusters();
    }




  }

  beforeDestroy() {
    this.$sstore.obs.clearTimer();
  }

  getPath(id: string) {
    return getPath(id)
  }

  updateCenter(center: any) {
    let query: any = {}
    if (this.$route.query.enabledCategories) {
      query['enabledCategories'] = this.$route.query.enabledCategories
    }
    if(this.$route.query.enabledTraficLights){
      query['enabledTraficLights'] = this.$route.query.enabledTraficLights
    }
    if (this.$route.query.coord) {
      query['zoom'] = this.$route.query.zoom
    }
    query['coord'] = center['lat'] + ',' + center['lng']

    this.$router.replace({
      name: 'datastreams',
      query: query
    }).catch(err => {
    })

  }

  updateZoom(zoom: any) {
    let query: any = {}
    if (this.$route.query.enabledCategories) {
      query['enabledCategories'] = this.$route.query.enabledCategories
    }
    if (this.$route.query.coord) {
      query['coord'] = this.$route.query.coord
    }
    if(this.$route.query.enabledTraficLights){
      query['enabledTraficLights'] = this.$route.query.enabledTraficLights
    }
    query['zoom'] = zoom

    this.$router.replace({
      name: 'datastreams',
      query: query
    }).catch(err => {
    })
  }

  swapCoords(coords: any) {
    //                    latitude , longitude, altitude
    //return new L.LatLng(coords[1], coords[0], coords[2]); //Normal behavior
    return new L.LatLng(coords[0], coords[1], coords[2]);
  }

   style(featureid:any) {


    if(featureid.split('~').reverse()[0]=='conflict' && this.$sstore.obs.state.obs[featureid]){
      console.log(this.$sstore.obs.state.obs[featureid]);
      if(this.$sstore.obs.state.obs[featureid].result){
        return (feature: any) => {
          return {
            weight: 2,
            color: "rgba(253,193,0,0.6)",
            opacity: 1,
            fillColor: 'rgba(253,193,0,0.6)',
            fillOpacity: 0.8
          };
        };
      }else {
        return {
          weight: 2,
          color: "rgba(222,220,220,0)",
          opacity: 1,
          fillColor: '#cccccc',
          fillOpacity: 0.8
        };
      }
    }
    //console.log(featureid)
    return (feature: any) => {
      return {
        weight: 2,
        color: "#ECEFF1",
        opacity: 1,
        fillColor: '#ccc',
        fillOpacity: 0.8
      };
    };
  }

  get pointToLayer() {
    return (feature: any, latlng: any) => {

      let div = document.createElement("div");
      let inner = document.createElement("div");
      let icon = document.createElement("div");
      icon.classList.add('icon');
      div.classList.add('marker');
      inner.classList.add('inner');
      div.append(inner);
      div.append(icon);
      if (feature.properties.heading) {

        const deg = (feature.properties.heading ?? 0) - 45;
        inner.style.webkitTransform = 'rotate(' + deg + 'deg)';
        //div.style.mozTransform    = 'rotate('+deg+'deg)';
        //div.style.msTransform     = 'rotate('+deg+'deg)';
        //div.style.oTransform      = 'rotate('+deg+'deg)';
        inner.style.transform = 'rotate(' + deg + 'deg)';
      }
      if(feature.properties.type){
        inner.classList.add('type_'+feature.properties.type);
        let innerHTML = feature.properties.type;
        switch (feature.properties.type){
          case '0':
            innerHTML = '<i class="mdi  mdi-walk"> </i>'
            break;

          case '1':
          case '2':
          case '3':
            innerHTML = '<i class="mdi mdi-bike"> </i>'
            break;
          case '5':
          case '6':
            innerHTML = '<i class="mdi mdi-car-side"> </i>'
            break;
          case '7':
            innerHTML = '<i class="mdi mdi-van-passenger"> </i>'
            break;
          case '10':
          case '11':
          case '12':
            innerHTML = '<i class="mdi mdi-truck"> </i>'
            break;
          case '14':
            innerHTML = '<i class="mdi mdi-bus-side"> </i>'
            break;


        }
        icon.innerHTML = innerHTML;
      }

      return new L.Marker(latlng, {
        icon:
          L.divIcon({className: 'my-div-icon', html: div})
      });
    }
  }

  get styleP() {

    return (feature: any) => {
      return {
        weight: 2,
        color: "#ECEFF1",
        opacity: 1,
        fillColor: '#ccc',
        fillOpacity: 0.8
      };
    };
  }

  get styleC() {

    return (feature: any) => {
      return {
        weight: 2,
        color: "#8c8c8c",

        opacity: 1,
        fillColor: '#e3e3e3',
        fillOpacity: 0.5
      };
    };
  }
  get camPointsSytle() {

    return (feature: any, latlng: any) => {
      let wrapper = document.createElement("div");
      let div = document.createElement("div");
      let inner = document.createElement("div");
      let icon = document.createElement("div");
      wrapper.classList.add('marker-wrapper','extSytle');
      icon.classList.add('icon');
      div.classList.add('marker-pin');
      inner.classList.add('inner','type_cam','round');
      icon.innerHTML = '<i class="mdi  mdi-camera-wireless"> </i>';
      div.append(inner);
      inner.append(icon);
      wrapper.append(div);

      return new L.Marker(latlng, {
        icon:
          L.divIcon({className: 'my-div-icon', html: wrapper})
      });
    };
  }

  SetobservationsGeoJsonMqtt() {


  }
  tooltip =  (feature:any, layer:any)=> {
    //console.log(feature)
    layer.bindTooltip(feature.properties.id, {
      direction: "left",
      permanent: true,
      className: 'labelstyle'
    });
    //layer.bindPopup("My popup content");
  }

  get isSelected(){
    return (point:any)=> {
      const arr = point['@iot.id'].splitt('~');
      return
    }
  }
}
</script>

<style scoped lang="scss">
@import "@/scss/general.scss";
@import "~leaflet.markercluster/dist/MarkerCluster.css";
@import "~leaflet.markercluster/dist/MarkerCluster.Default.css";

#map {
  /*position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;*/
}

.grid {
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: 55px 1fr;
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: #272727;
}

.rim {
  padding: 5px;
}

.sidebar_holder {
  bottom: 0;
  left: 0;
  width: 350px;
  top: 55px;
  z-index: 5;
  background: #3a3a3af7;
  box-shadow: 5px 6px 5px #0000003d;

  .plane {
    padding: 25px 0px 5px 0px;
    background: transparent;
  }
}

.marker-pin {
  width: 40px;
  height: 40px;
  border-radius: 50% 50% 50% 0;
  background: #002770;
  position: absolute;
  transform: rotate(-45deg);
  left: 50%;
  top: 50%;
  margin: -15px 0 0 -15px;
  box-shadow: -8px 15px 15px 0px rgb(0 0 0 / 43%);


}

.marker-pin.selected {
  .round {
    background: $bs-blue;
  }

  .svg_icon {
    &.dark {
      background: #fcfcfc;
    }
  }

}

// to draw white circle
.marker-pin::after {
  content: "";
  width: 24px;
  height: 24px;
  margin: 3px 0 0 -12px;
  //background: #fff;
  position: absolute;
  border-radius: 50%;
  transform: rotate(-45deg);
  //box-shadow: inset 0px 0px 3px 0px #00000078;
}

// to align icon
.custom-div-icon i {
  position: absolute;
  width: 22px;
  font-size: 22px;
  left: 0;
  right: 0;
  margin: -1px 3px;
  text-align: center;
  color: $primary;
  transform: rotate(45deg);
}

.custom-div-icon .marker-pin.selected i {
  color: #fff;
}

.colspan2 {
  grid-column: 1 / 3;
}

.marker-value {
  position: absolute;
  left: 12px;
  border: 1px solid #ccc;
  top: -10px;
  background: #fff;
  padding: 4px;
  border-radius: 21px;
  /* box-shadow: 3px 14px 15px 0px rgba(0, 0, 0, 0.43); */
  font-size: 16px;
  &.color{

    left: -35px;
    top: -1px;
    transform: rotate(-90deg);
  }
  &.conflict {
    border:none;
    padding:0;
    .is_set{
      //animation: blinker 2s step-start infinite;
    border-radius:100%;

      width:24px;
      font-weight:500;
    //border: 1px dashed #bdbdbd;
    background: #e4a53b;

    }


    /* box-shadow: 3px 14px 15px 0px rgba(0, 0, 0, 0.43); */

  }
  &.viewport{
    display:none;
  }
}

.round {
  width: 34px;
  height: 34px;
  border-radius: 100%;
  transform: rotate(45deg);
  margin: 3px;
  background: #ffffff;
  display: flex;
  font-size: 21px;
  flex-direction: row;
  align-content: center;
  justify-content: center;
  align-items: center;
}

#logo {
  width: 237px;
  height: 97px;
  position: absolute;
  left: 3px;
  top: -20px;
  background-size: 138px;
  background-position: 0px;
}

#claim {
  width: 131px;
  height: 97px;
  position: absolute;
  left: 49px;
  top: -50px;
  background-size: 240px;
  background-position: -109px;
}

.logo {
  overflow: hidden;
  height: 56px;
  width: 214px;
  position: relative;
}

.titlebar {

  background: $bs-blue;
  height: 100%;
  display: flex;
  flex-direction: row;
  align-content: center;
  justify-content: flex-start;
  align-items: center;
  box-shadow: 0px 12px 14px #00000059;
  z-index: 7;
  position: relative;
}

.map_holder {
  z-index: 5;
  position: relative;
}

.propertie_holder {
  bottom: 0;
  left: 350px;
  /* width: 350px; */
  height: 351px;
  right: 0;
  z-index: 6;
  background: rgba(58, 58, 58, 0.968627451);
  box-shadow: 5px 6px 5px rgba(0, 0, 0, 0.2392156863);
}

.svg_icon {
  width: 24px;
  height: 24px;

  &.dark {
    background: #363636;
  }

}

.absbtn {
  position: absolute;
  right: 0;
  top: 5px;
  z-index: 1141;
}
.marker_svg{
  color:#333;
}
</style>
<style lang="scss">
.extSytle{


  .round {
    width: 34px;
    height: 34px;
    border-radius: 100%;
    transform: rotate(45deg);
    margin: 3px;
    background: #ffffff;
    display: flex;
    font-size: 21px;
    flex-direction: row;
    align-content: center;
    justify-content: center;
    align-items: center;
  }

  .marker-pin {
    width: 40px;
    height: 40px;
    border-radius: 50% 50% 50% 0;
    background: #002770;
    position: absolute;
    transform: rotate(-45deg);
    left: 50%;
    top: 50%;
    margin: -15px 0 0 -15px;
    box-shadow: -8px 15px 15px 0px rgb(0 0 0 / 43%);


  }

  .marker-pin.selected {
    .round {
      background: #002770;
    }

    .svg_icon {
      &.dark {
        background: #fcfcfc;
      }
    }

  }

  // to draw white circle
  .marker-pin::after {
    content: "";
    width: 24px;
    height: 24px;
    margin: 3px 0 0 -12px;
    //background: #fff;
    position: absolute;
    border-radius: 50%;
    transform: rotate(-45deg);
    //box-shadow: inset 0px 0px 3px 0px #00000078;
  }
}
@keyframes blinker {
  50% {
    opacity: 0;
  }
}
#app {
  .marker-cluster {
    background-color: rgb(27 52 111 / 19%);

    div {
      background-color: rgb(27 52 111 / 100%);
      color: #fcfcfc;
    }
  }
}
.marker{

  //box-shadow: 5px 3px 9px rgba(0, 0, 0, 0.2980392157);
  //width: 25px;
  box-shadow: 5px 3px 6px -1px rgb(0 0 0 / 45%);
  height: 25px;
  /* background: transparent; */
  border-radius: 100%;
  .inner{
    width:25px;
    height: 25px;
    background: #2058a2;
    border-top-left-radius: 100%;
    border-bottom-left-radius: 100%;
    border-bottom-right-radius: 100%;

    &.type_cam{ //Pad
      background: rgba(126, 194, 243);
      border-radius: 0;
    }

    &.type_0{ //Pad
      background: #2058a2;
    }
    &.type_1, &.type_2, &.type_3{ //bike
      background: #6820a2;
    }
    &.type_4, &.type_5, &.type_6, &.type_7{ //car
      background: #20a29e;
    }
    &.type_8, &.type_9, &.type_10, &.type_11,&.type_12{ //car
      background: #a24720;
    }
  }
  .icon{
    position: absolute;
    top: 0;
    left: 0;
    color: #fff;
    font-size: 21px;
  }
}
</style>
