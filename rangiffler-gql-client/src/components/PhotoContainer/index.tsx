import Container from "@mui/material/Container";
import Grid from "@mui/material/Grid";
import { FC } from "react";
import { PhotoCard } from "../PhotoCard";
import { Photo } from "../../types/Photo";
import {PhotoPagination} from "../PhotoPagination";

interface PhotoContainerInterface {
    data: Photo[];
    onSelectImage: (photo: Photo) => void;
    page: number;
    hasPreviousPage: boolean;
    hasNextPage: boolean;
    setPage: (page: number) => void;
}
export const PhotoContainer: FC<PhotoContainerInterface> = ({onSelectImage, data, page, setPage, hasPreviousPage, hasNextPage}) => {

    return (
        <Container>
            <Grid container spacing={3}>
                {data.map((item: Photo) => (
                    <Grid item key={item.id} xs={3}>
                    <PhotoCard
                        photo={item}
                        onEditClick={onSelectImage}
                    />
                    </Grid>
                ))}
            </Grid>
            {(hasNextPage || hasPreviousPage) &&
                <PhotoPagination
                    onPreviousClick={() => setPage(page - 1)}
                    onNextClick={() => setPage(page + 1)}
                    hasPreviousValues={hasPreviousPage}
                    hasNextValues={hasNextPage}
                />
            }
        </Container>
    );
};