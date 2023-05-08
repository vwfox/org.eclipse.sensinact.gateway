let config= {
  baseUrl:window.location.origin+'/sensinact/rest',
  //baseUrl: 'http://localhost:8080/sensinact/rest'
};

export function setBaseUrl(url:string){
  config.baseUrl = url;
}
export function getBaseUrl(){
  return config.baseUrl;
}
