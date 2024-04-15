import {FC } from 'react';
import SvgWorldMap, {CountryContext} from 'react-svg-worldmap';
import "./styles.css";

type WorldMapData = {
    country: {
        code: string,
    },
    count: number,
}
interface WorldMapInterface {
    data: WorldMapData[],
    photoFilter: string | null,
    setPhotoFilter: (photoFilter: string | null) => void,
    mapTitle: string,
    setMapTitle: (mapTitle: string) => void,
}
export const WorldMap: FC<WorldMapInterface> = ({data = [], setPhotoFilter, mapTitle, setMapTitle}) => {
    const mapData = data.map((v) => ({
        country: v.country.code,
        value: v.count,
    }));

    const handleCountryClick = (context: CountryContext) => {
        setMapTitle(context.countryName);
        setPhotoFilter(context.countryCode);
    };

    return (
            <SvgWorldMap
                color="#174536"
                value-suffix="people"
                size="xl"
                title={mapTitle}
                data={mapData}
                richInteraction={true}
                onClickFunction={handleCountryClick}
            />
    );
}
