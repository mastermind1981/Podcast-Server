/**
 * Created by kevin on 25/10/2015 for PodcastServer
 */
import {Component, Module} from '../../decorators';
import Videogular from '../../common/component/videogular/videogular';
import AppRouteConfig from '../../config/route';
import template from './item-player.html!text';

@Module({
    name : 'ps.item.player',
    modules : [ AppRouteConfig, Videogular ]
})
@Component({
    selector : 'item-player',
    as : 'ipc',
    template : template,

    path : '/podcasts/:podcastId/item/:itemId/play',
    resolve : {
        item : (itemService, $route) => {"ngInject"; return itemService.findById($route.current.params.podcastId, $route.current.params.itemId);},
        podcast : (podcastService, $route) => {"ngInject"; return podcastService.findById($route.current.params.podcastId);}
    }
})
export default class ItemPlayerController {

    constructor(VideogularService) {
        "ngInject";
        this.VideogularService = VideogularService;
    }

    $onInit() {
        this.item.podcast = this.podcast;
        this.config = this.VideogularService.builder().withItem(this.item).build();
    }
}
