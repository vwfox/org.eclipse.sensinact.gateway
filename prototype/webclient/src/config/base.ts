let config= {
  baseUrl:window.location.origin+'/sensinact/rest',
  //baseUrl: 'https://udp-5g-broker.nomad-dmz.jena.de/sensinact/rest'
};

export function setBaseUrl(url:string){
  config.baseUrl = url;
}
export function getBaseUrl(){
  return config.baseUrl;
}
