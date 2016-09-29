package lan.dk.podcastserver.manager.worker.selector;

import com.google.common.collect.Sets;
import lan.dk.podcastserver.manager.worker.updater.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdaterSelectorTest {

    private UpdaterSelector updaterSelector;

    @Mock YoutubeUpdater youtubeUpdater;
    @Mock RSSUpdater rssUpdater;
    @Mock BeInSportsUpdater beInSportsUpdater;
    @Mock CanalPlusUpdater canalPlusUpdater;
    @Mock JeuxVideoComUpdater jeuxVideoComUpdater;
    @Mock PluzzUpdater pluzzUpdater;
    @Mock DailymotionUpdater dailymotionUpdater;

    @Before
    public void setUp() throws Exception {
        /* Given */
        when(youtubeUpdater.compatibility(anyString())).thenCallRealMethod();
        when(rssUpdater.compatibility(anyString())).thenCallRealMethod();
        when(beInSportsUpdater.compatibility(anyString())).thenCallRealMethod();
        when(canalPlusUpdater.compatibility(anyString())).thenCallRealMethod();
        when(jeuxVideoComUpdater.compatibility(anyString())).thenCallRealMethod();
        when(pluzzUpdater.compatibility(anyString())).thenCallRealMethod();
        when(dailymotionUpdater.compatibility(anyString())).thenCallRealMethod();

        when(youtubeUpdater.type()).thenCallRealMethod();
        when(rssUpdater.type()).thenCallRealMethod();
        when(beInSportsUpdater.type()).thenCallRealMethod();
        when(canalPlusUpdater.type()).thenCallRealMethod();
        when(jeuxVideoComUpdater.type()).thenCallRealMethod();
        when(pluzzUpdater.type()).thenCallRealMethod();
        when(dailymotionUpdater.type()).thenCallRealMethod();

        //updaterSelector = new UpdaterSelector();
        //updaterSelector.setUpdaters(Sets.newHashSet(youtubeUpdater, rssUpdater, beInSportsUpdater, canalPlusUpdater, jeuxVideoComUpdater, parleysUpdater, pluzzUpdater, dailymotionUpdater));
        updaterSelector = new UpdaterSelector(Sets.newHashSet(youtubeUpdater, rssUpdater, beInSportsUpdater, canalPlusUpdater, jeuxVideoComUpdater, pluzzUpdater, dailymotionUpdater));
    }
    
    @Test
    public void should_return_an_RssUpdater () {
        /* When */ Updater updaterClass = updaterSelector.of("www.link.to.rss/feeds");
        /* Then */ assertThat(updaterClass).isEqualTo(rssUpdater);
    }

    @Test
    public void should_return_a_YoutubeUpdater () {
        /* When */ Updater updaterClass = updaterSelector.of("http://www.youtube.com/user/fakeUser");
        /* Then */ assertThat(updaterClass).isEqualTo(youtubeUpdater);
    }

    @Test
    public void should_return_a_BeInSportUpdater () {
        /* When */ Updater updaterClass = updaterSelector.of("http://www.beinsports.com/replay/category/3361/name/lexpresso");
        /* Then */ assertThat(updaterClass).isEqualTo(beInSportsUpdater);
    }

    @Test
    public void should_return_a_CanalPlusUpdater() {
        /* When */ Updater updaterClass = updaterSelector.of("http://www.canalplus.fr/show/for/dummies");
        /* Then */ assertThat(updaterClass).isEqualTo(canalPlusUpdater);
    }

    @Test
    public void should_return_a_JeuxVideoComUpdater() {
        /* When */ Updater updaterClass = updaterSelector.of("http://www.jeuxvideo.com/show/for/dummies");
        /* Then */ assertThat(updaterClass).isEqualTo(jeuxVideoComUpdater);
    }

    @Test
    public void should_return_a_PluzzUpdater() {
        /* When */ Updater updaterClass = updaterSelector.of("http://www.pluzz.francetv.fr/show/for/dummies");
        /* Then */ assertThat(updaterClass).isEqualTo(pluzzUpdater);
    }

    @Test
    public void should_return_a_DailymotionUpdater() {
        /* When */ Updater updaterClass = updaterSelector.of("http://www.dailymotion.com/showname");
        /* Then */ assertThat(updaterClass).isEqualTo(dailymotionUpdater);
    }

    @Test
    public void should_reject_empty_url() {
        /* When */
        assertThat(updaterSelector.of("")).isEqualTo(UpdaterSelector.NO_OP_UPDATER);
    }

    @Test
    public void should_serve_types() {
        /* When */
        Set<AbstractUpdater.Type> types = updaterSelector.types();

        /* Then */
        assertThat(types).isNotEmpty().hasSize(7);
    }

}