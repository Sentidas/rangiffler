import { gql, useMutation } from "@apollo/client";

interface PhotoInput {
    variables: {
        input: {
            src: string,
            description: string,
            country: {
                code: string,
            }
        }
    }
}

const CREATE_PHOTO = gql(`
    mutation CreatePhoto($input: PhotoInput!) {
        photo(input: $input) {
            id
            src
            country {
                code
                name
                flag
            }
            description
            likes {
                total
            }
        }
    }
`);

type CreatePhotoRequestType = {
    onError: () => void,
    onCompleted: () => void,
}

type CreatePhotoReturnType = {
    createPhoto: (updateUserInput: PhotoInput) => void,
    loading: boolean,
}

export const useCreatePhoto = (req: CreatePhotoRequestType) : CreatePhotoReturnType => {
    const [createPhoto, {loading}] = useMutation(CREATE_PHOTO, {
        refetchQueries: [
            'GetFeed'
        ],
        onError: req.onError,
        onCompleted: req.onCompleted,
    });
    return {createPhoto, loading};
};