import {Box, Button, Container, Typography} from "@mui/material";
import {PhotoContainer} from "../../components/PhotoContainer";
import {WorldMap} from "../../components/WorldMap";
import {Toggle} from "../../components/Toggle";
import {useMemo, useState} from "react";
import { PhotoModal } from "../../components/PhotoModal";
import {PhotoFormProps, formInitialState} from "../../components/PhotoModal/formValidate";
import { Photo } from "../../types/Photo";
import { useGetFeed } from "../../hooks/useGetFeed";

export const MyTravelsPage = () => {
    const [modalState, setModalState] = useState<{ isVisible: boolean, formData: PhotoFormProps | null, photoId: string | null,}>({
        isVisible: false,
        formData: null,
        photoId: null,
    });

    const [withMyFriends, setWithMyFriends] = useState(false);
    const [page, setPage] = useState(0);
    const {photos, stat, hasPreviousPage, hasNextPage} = useGetFeed({page, withFriends: withMyFriends});
    const [photoFilter, setPhotoFilter] = useState<string | null>(null);
    const filteredPhotos = useMemo<Photo[]>(
        () => photoFilter ? (photos as Photo[]).filter(photo => photo.country.code.toLowerCase() === photoFilter.toLowerCase()) : photos
        , [photoFilter, photos]);
    const [mapTitle, setMapTitle] = useState<string>("All countries");

    const handleAllCountriesClick = () => {
        setMapTitle("All countries")
        setPhotoFilter(null);
    };
    const handleSelectImage = (photo: Photo) => {
        setModalState({
            isVisible: true,
            formData: {...formInitialState,
                    description: {
                    ...formInitialState.description,
                        value: photo.description
                    },
                    src: {
                    ...formInitialState.src,
                        value: photo.src,
                    }
                },
            photoId: photo.id
        });
    }

    return (
        <Container sx={{
            height: '100%',
            paddingBottom: 5,
        }}>
            <Typography
                variant="h5"
                component="h2"
                sx={{
                    marginBottom: 2,
                }}
            >
                Travels map
            </Typography>
            <Box sx={{
                display: "flex",
                justifyContent: "space-between",
                alignItens: "center",
            }}>
                <Box sx={{
                    display: "flex",
                    flexDirection: 'column',
                }}>
                    <Box sx={{
                        margin: 1,
                    }}>
                        <Toggle withMyFriends={withMyFriends} setWithMyFriends={setWithMyFriends}/>
                    </Box>
                </Box>
                <WorldMap
                    mapTitle={mapTitle}
                    setMapTitle={setMapTitle}
                    photoFilter={photoFilter}
                    setPhotoFilter={setPhotoFilter}
                    data={stat}
                />
                <Box sx={{width: 180}}>
                    <Button
                        variant="contained"
                        sx={{
                            width: "100%",
                            margin: 1,
                            marginLeft: "auto",
                        }}
                        onClick={() => {
                            setModalState({
                                isVisible: true,
                                formData: null,
                                photoId: null,
                            })
                        }}
                    >Add photo
                    </Button>
                    <Button
                        disabled={photoFilter===null}
                        variant="outlined"
                        sx={{
                            width: "100%",
                            margin: 1,
                            marginLeft: "auto",
                        }}
                        onClick={handleAllCountriesClick}
                    >All countries
                    </Button>
                </Box>
            </Box>
            <PhotoContainer
                onSelectImage={handleSelectImage}
                data={filteredPhotos}
                page={page}
                setPage={setPage}
                hasNextPage={hasNextPage}
                hasPreviousPage={hasPreviousPage}
            />
            <PhotoModal
                modalState={modalState}
                isEdit={!!(modalState.formData)}
                onClose={() => {
                    setModalState({
                        isVisible: false,
                        formData: null,
                        photoId: modalState.photoId,
                    });
                }}
            />
        </Container>
    )
}

