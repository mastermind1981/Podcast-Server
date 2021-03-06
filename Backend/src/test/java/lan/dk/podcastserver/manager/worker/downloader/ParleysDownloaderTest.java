package lan.dk.podcastserver.manager.worker.downloader;

import com.github.axet.wget.WGet;
import com.github.axet.wget.info.DownloadInfo;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lan.dk.podcastserver.entity.Item;
import lan.dk.podcastserver.entity.Podcast;
import lan.dk.podcastserver.entity.Status;
import lan.dk.podcastserver.manager.ItemDownloadManager;
import lan.dk.podcastserver.repository.ItemRepository;
import lan.dk.podcastserver.repository.PodcastRepository;
import lan.dk.podcastserver.service.FfmpegService;
import lan.dk.podcastserver.service.JsonService;
import lan.dk.podcastserver.service.MimeTypeService;
import lan.dk.podcastserver.service.factory.WGetFactory;
import lan.dk.podcastserver.service.properties.PodcastServerParameters;
import lan.dk.utils.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by kevin on 19/03/2016 for Podcast Server
 */
@RunWith(MockitoJUnitRunner.class)
public class ParleysDownloaderTest {

    private static final String ROOT_FOLDER = "/tmp";
    private static final String ASSETS_URL = "https://cdn.parleys.com/p/5534a6b4e4b056a82338229d/9Wwo6oOVmk3_177812_60938.mp4?Signature=Rc-FUFrD81ypdP4RKtoAMr2E3RRf-dFYAbrW9jnAstX-R1S9-lgOrZBvpLRaVdTeOKMB-Td4tkag3doUIAjEcwFXzW3EWp-Zq0htHC0RhZWCb~LxVcsyzBHo6nYpyy0V4V--44CaumrgBV-~utssWmisLU5bzmhelySHyHTCrhtVo3CAZpSXsDBIWL7gK8jZ5pB61zJtHQiRMhSFC6bjcHlEJByftM83sr9R-U8GgdtTa6t1FRjCeVXYmKKG~1snsUz9mtSLYVrEGzxz3NojtJlVuAWtD7FQPEISHI1EMxDb9cWGoL8RijXVEjyfzUNmDJTwBNMKECwWM732DhUuBg__&Policy=eyJTdGF0ZW1lbnQiOlt7IlJlc291cmNlIjoiaHR0cHM6Ly9jZG4ucGFybGV5cy5jb20vcC81NTM0YTZiNGU0YjA1NmE4MjMzODIyOWQvOVd3bzZvT1ZtazNfMTc3ODEyXzYwOTM4Lm1wND9yZXNwb25zZS1jb250ZW50LWRpc3Bvc2l0aW9uPWF0dGFjaG1lbnQlM0JmaWxlbmFtZSUzRCUyNzlXd282b09WbWszXzE3NzgxMl82MDkzOC5tcDQlMjciLCJDb25kaXRpb24iOnsiRGF0ZUxlc3NUaGFuIjp7IkFXUzpFcG9jaFRpbWUiOjE0NDk0MDg1MzR9fX1dfQ__&Key-Pair-Id=APKAIUQAZRCNZDWJ4JJQ&response-content-disposition=attachment%3Bfilename%3D%279Wwo6oOVmk3_177812_60938.mp4%27";
    private static final ParseContext PARSER = JsonPath.using(Configuration.builder().mappingProvider(new JacksonMappingProvider()).build());

    @Mock PodcastRepository podcastRepository;
    @Mock ItemRepository itemRepository;
    @Mock ItemDownloadManager itemDownloadManager;
    @Mock PodcastServerParameters podcastServerParameters;
    @Mock SimpMessagingTemplate template;
    @Mock MimeTypeService mimeTypeService;

    @Mock JsonService jsonService;
    @Mock FfmpegService ffmpegService;
    @Mock WGetFactory wGetFactory;

    @InjectMocks ParleysDownloader parleysDownloader;

    @Captor ArgumentCaptor<Path> toConcatFiles;

    Podcast podcast;
    Item item;

    @Before
    public void beforeEach() {
        podcast = Podcast.builder()
                .id(UUID.randomUUID())
                .title("ParleysPodcast")
                .build();
        item = Item.builder()
                    .id(UUID.randomUUID())
                    .podcast(podcast)
                    .url("http://www.parleys.com/play/5534a6b4e4b056a82338229d")
                .build();


        when(podcastRepository.findOne(eq(podcast.getId()))).thenReturn(podcast);
        when(podcastServerParameters.getRootfolder()).thenReturn(Paths.get(ROOT_FOLDER));
        when(podcastServerParameters.getDownloadExtension()).thenReturn(".psdownload");
        doAnswer(i -> {
            Files.createFile(Path.class.cast(i.getArguments()[0]));
            return null;
        }).when(ffmpegService).concat(any(Path.class), anyVararg());

        parleysDownloader.postConstruct();
        parleysDownloader.setItem(item);
        parleysDownloader.setItemDownloadManager(itemDownloadManager);

        FileSystemUtils.deleteRecursively(Paths.get(ROOT_FOLDER, podcast.getTitle()).toFile());
    }

    @Test
    public void should_download() throws MalformedURLException {
        /* Given */
        DownloadInfo mock = mock(DownloadInfo.class);
        WGet wget = mock(WGet.class);

        when(jsonService.parseUrl(eq("http://api.parleys.com/api/presentation.json/5534a6b4e4b056a82338229d?view=true")))
                .then(i -> IOUtils.fileAsJson("/remote/podcast/parleys/5534a6b4e4b056a82338229d.json"));
        when(wGetFactory.newDownloadInfo(anyString())).thenReturn(mock);
        when(wGetFactory.newWGet(any(DownloadInfo.class), any(File.class))).thenReturn(wget);

        /* When */
        parleysDownloader.download();

        /* Then */
        assertThat(parleysDownloader.target)
                .exists()
                .hasFileName("5534a6b4e4b056a82338229d.mp4")
                .hasParent(Paths.get(ROOT_FOLDER, podcast.getTitle()));
        assertThat(item.getStatus()).isSameAs(Status.FINISH);
        verify(ffmpegService, times(1)).concat(eq(parleysDownloader.target), toConcatFiles.capture());
        assertThat(toConcatFiles.getAllValues()).hasSize(3)
                .containsExactly(
                        Paths.get("/tmp/ParleysPodcast/9Wwo6oOVmk3_177812_60938.mp4"),
                        Paths.get("/tmp/ParleysPodcast/X3yV1bXkPep_238750_2679062.mp4"),
                        Paths.get("/tmp/ParleysPodcast/9Wwo6oOVmk3_2917812_8125.mp4")
                );
    }

    @Test
    public void should_not_found_any_item() {
        /* Given */
        item.setUrl("http://a.wrong.url/");
        /* When */
        parleysDownloader.download();

        /* Then */
        assertThat(item.getStatus()).isSameAs(Status.STOPPED);
    }

    @Test
    public void should_handle_assets_with_wrong_url() throws MalformedURLException {
         /* Given */
        DownloadInfo mock = mock(DownloadInfo.class);
        WGet wget = mock(WGet.class);

        when(jsonService.parseUrl(eq("http://api.parleys.com/api/presentation.json/5534a6b4e4b056a82338229d?view=true")))
                .then(i -> IOUtils.fileAsJson("/remote/podcast/parleys/5534a6b4e4b056a82338229d.json"));
        doThrow(MalformedURLException.class).when(wGetFactory).newDownloadInfo(eq(ASSETS_URL));
        when(wGetFactory.newDownloadInfo(not(eq(ASSETS_URL)))).thenReturn(mock);
        when(wGetFactory.newWGet(any(DownloadInfo.class), any(File.class))).thenReturn(wget);

        /* When */
        parleysDownloader.download();

        /* Then */
        assertThat(parleysDownloader.target)
                .exists()
                .hasFileName("5534a6b4e4b056a82338229d.mp4")
                .hasParent(Paths.get(ROOT_FOLDER, podcast.getTitle()));
        assertThat(item.getStatus()).isSameAs(Status.FINISH);
        verify(ffmpegService, times(1)).concat(eq(parleysDownloader.target), toConcatFiles.capture());
        assertThat(toConcatFiles.getAllValues()).hasSize(2)
                .containsExactly(
                        Paths.get("/tmp/ParleysPodcast/X3yV1bXkPep_238750_2679062.mp4"),
                        Paths.get("/tmp/ParleysPodcast/9Wwo6oOVmk3_2917812_8125.mp4")
                );
    }

    @Test
    public void should_be_compatible() {
        assertThat(parleysDownloader.compatibility(item.getUrl())).isEqualTo(1);
    }

    private Answer<Optional<Object>> readerFrom(String url) {
        return i -> Optional.of(PARSER.parse(Paths.get(ParleysDownloaderTest.class.getResource(url).toURI()).toFile()));
    }

}