package sanekp.seriesinformer.core.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import sanekp.seriesinformer.core.model.SeriesDto;
import sanekp.seriesinformer.core.model.Viewed;
import sanekp.seriesinformer.core.xml.ObjectFactory;
import sanekp.seriesinformer.core.xml.Series;
import sanekp.seriesinformer.core.xml.SeriesList;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sanek_000 on 5/25/2014.
 */
@Component
public class DbManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbManager.class);

    private Unmarshaller unmarshaller;
    private Marshaller marshaller;
    private SimpleDateFormat simpleDateFormat;
    private List<SeriesDto> seriesDtos;

    @Value("${db.path}")
    private String dbPath;

    @Value("commit.log")
    private String commitLog;

    @Autowired
    private ConversionService conversionService;

    @PostConstruct
    public void init() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://seriesinformer.sanekp dbschema.xsd");
        unmarshaller = jaxbContext.createUnmarshaller();

        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        reload();
    }

    private void reload() {
        try {
            SeriesList seriesList = (SeriesList) unmarshaller.unmarshal(new File(dbPath));
            LOGGER.debug("Loaded {} series", seriesList.getSeries().size());
            seriesDtos = new ArrayList<>(seriesList.getSeries().size());
            for (Series series : seriesList.getSeries()) {
                seriesDtos.add(conversionService.convert(series, SeriesDto.class));
            }
        } catch (JAXBException e) {
            LOGGER.warn("Failed to load series", e);
        }
    }

    public void update(SeriesDto seriesDto) {
//        reload();
        Viewed viewed = seriesDto.getViewed();
        try (FileWriter fileWriter = new FileWriter(commitLog, true)) {
            fileWriter.write(simpleDateFormat.format(new Date()) + "," + seriesDto.getName() + "," + viewed.getSeason() + "," + viewed.getEpisode() + "\n");
            SeriesList seriesList = (SeriesList) unmarshaller.unmarshal(new File(dbPath));
            for (Series series : seriesList.getSeries()) {
                if (series.getName().equals(seriesDto.getName())
                        && (series.getSeason() < viewed.getSeason()
                        || series.getEpisode() < viewed.getEpisode())) {
                    series.setSeason(viewed.getSeason());
                    series.setEpisode(viewed.getEpisode());
                    marshaller.marshal(seriesList, new File(dbPath));
                    break;
                }
            }
            LOGGER.debug("Updated {}", seriesDto.getName());
        } catch (JAXBException | IOException e) {
            LOGGER.warn("Failed to store series", e);
        }
    }

    public List<SeriesDto> getSeriesDtos() {
        return seriesDtos;
    }
}
