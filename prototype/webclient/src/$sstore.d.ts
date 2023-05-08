import Vue from "vue";
import {simpleStoreIF} from "@/simpleStore/SimpleStore";

declare module 'vue/types/vue' {

    interface Vue {
        $sstore: simpleStoreIF;
    }
}