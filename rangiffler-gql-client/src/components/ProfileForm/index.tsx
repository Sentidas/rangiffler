import {
    Box,
    Button,
    FormControl,
    Grid,
    InputLabel,
    MenuItem,
    OutlinedInput,
    Select,
    SelectChangeEvent,
    TextField,
    Typography
} from "@mui/material"
import {useCountries} from "../../context/CountriesContext";
import {MenuProps} from "../CountrySelect";
import {ChangeEvent, FormEvent, useContext, useState} from "react";
import {ImageUpload} from "../ImageUpload";
import {formInitialState, formValidate, UserFormProps} from "./formValidate";
import {formHasErrors} from "../PhotoModal/formValidate";
import {useUpdateUser} from "../../hooks/useUpdateUser";
import {useSnackBar} from "../../context/SnackBarContext";
import {SessionContext} from "../../context/SessionContext";

/* -------------------- ДОБАВЛЕНО: парсеры "суффиксов" ошибок с бэка -------------------- */
function limitSuffixFromBackend(e: any): string {
    const result = e?.networkError?.result;
    if (result?.error === 'PAYLOAD_TOO_LARGE_STRING') {
        const msg = typeof result?.message === 'string' ? result.message.trim() : '';
        return msg || 'Limit exceeded';
    }
    return '';
}

function unsupportedFormatSuffixFromBackend(e: any): string {
    // 1) Вариант REST с телом
    const result = e?.networkError?.result;
    if (result?.error === 'UNSUPPORTED_IMAGE_FORMAT') {
        const msg = typeof result?.message === 'string' ? result.message.trim() : '';
        return msg || 'Unsupported image format';
    }
    // 2) Вариант GraphQL (HTTP 200, ошибки в graphQLErrors[])
    const gqlErrors: any[] = Array.isArray(e?.graphQLErrors) ? e.graphQLErrors : [];
    for (const err of gqlErrors) {
        const ext = err?.extensions;
        if (ext?.error === 'UNSUPPORTED_IMAGE_FORMAT') {
            const msg = typeof err?.message === 'string' ? err.message.trim() : '';
            return msg || 'Unsupported image format';
        }
    }
    // 3) «Голый» 415 без тела
    const status = e?.networkError?.status ?? e?.networkError?.statusCode;
    if (status === 415) return 'Unsupported image format';
    return '';
}

function authSuffixFromBackend(e: any): string {
    const r = e?.networkError?.result;
    if (r?.error === 'UNAUTHORIZED') {
        const msg = typeof r?.message === 'string' ? r.message.trim() : '';
        return msg || 'Authorization required';
    }
    const status = e?.networkError?.status ?? e?.networkError?.statusCode;
    if (status === 401) return 'Authorization required';
    // GraphQL-вариант (HTTP 200, ошибки в graphQLErrors[])
    const gqlErrors: any[] = Array.isArray(e?.graphQLErrors) ? e.graphQLErrors : [];
    for (const err of gqlErrors) {
        const ext = err?.extensions;
        if (ext?.error === 'UNAUTHORIZED') {
            const msg = typeof err?.message === 'string' ? err.message.trim() : '';
            return msg || 'Authorization required';
        }
    }
    return '';
}
/* -------------------- КОНЕЦ: парсеры "суффиксов" ошибок с бэка -------------------- */

export const ProfileForm = () => {
    const {countries} = useCountries();
    const {user} = useContext(SessionContext);
    const snackbar = useSnackBar();


    // const {updateUser} = useUpdateUser({
    //     onError: () => snackbar.showSnackBar("Can not update user", "error"),
    //     onCompleted: () => snackbar.showSnackBar("Your profile is successfully updated", "success"),
    // });

    const {updateUser} = useUpdateUser({
        onError: (e: any) => {
            const base = "Can not update user";
            const limit = limitSuffixFromBackend(e);
            const auth = authSuffixFromBackend(e);
            const unsupported = unsupportedFormatSuffixFromBackend(e);
            const suffix = limit || auth || unsupported;
            snackbar.showSnackBar(suffix ? `${base}. ${suffix}` : base, "error");
        },
        onCompleted: () => snackbar.showSnackBar("Your profile is successfully updated", "success"),
    });

    const getInitialState = () => ({
        ...formInitialState,
        firstname: {...formInitialState.firstname, value: user?.firstname ?? ""},
        surname: {...formInitialState.surname, value: user?.surname ?? ""},
        username: {...formInitialState.username, value: user?.username ?? ""},
        location: {...formInitialState.location, value: user?.location.code ?? "ru"},
        avatar: {...formInitialState.avatar, value: user?.avatar ?? undefined}
    });

    const [formValues, setFormValues] = useState<UserFormProps>(getInitialState());

    const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
        const {name, value} = event.target;
        setFormValues({
            ...formValues,
            [name]: {
                ...formValues[name],
                value,
            }
        })
    };

    const handleSelectValueChange = (event: SelectChangeEvent<string>) => {
        const {name, value} = event.target;
        setFormValues({
            ...formValues,
            [name]: {
                ...formValues[name],
                value,
            }
        })
    };

    const onSubmit = (e: FormEvent) => {
        e.preventDefault();
        const validatedData = formValidate(formValues);
        setFormValues(validatedData);
        if (!formHasErrors(validatedData)) {
            updateUser({
                variables: {
                    input: {
                        firstname: formValues.firstname.value,
                        surname: formValues.surname.value,
                        avatar: formValues.avatar.value,
                        location: {
                            code: formValues.location.value,
                        }
                    }
                }
            });
        }
    };

    const onReset = () => {
        setFormValues(getInitialState());
    };

    return (
        <Grid
            container
            component="form"
            onSubmit={onSubmit}
            spacing={0}
            sx={{
                marginTop: 5,
            }}
        >
            <Grid item xs={4} sx={{
                display: "flex",
                justifyContent: "center",
            }}>
                <ImageUpload
                    buttonText="Update avatar"
                    file={formValues.avatar.value}
                    error={formValues.avatar.error}
                    helperText={formValues.avatar.error ? formValues.avatar.errorMessage : ""}
                    isAvatar
                    onFileUpload={(file) => {
                        setFormValues({
                            ...formValues, avatar: {
                                ...formValues.avatar,
                                value: file,
                            }
                        })
                    }}/>
            </Grid>
            <Grid item xs={8}>
                <Typography
                    variant="h4"
                    component="h2"
                    sx={{
                        marginBottom: 3,
                    }}
                >
                    My profile
                </Typography>
                <Grid
                    container
                    spacing={3}
                >
                    <Grid item xs={6}>
                        <TextField
                            id="firstname"
                            name="firstname"
                            label="First Name"
                            type="text"
                            value={formValues?.firstname.value}
                            error={formValues.firstname.error}
                            helperText={formValues.firstname.error && formValues.firstname.errorMessage}
                            onChange={handleChange}
                            fullWidth
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            id="surname"
                            name="surname"
                            label="Surname"
                            type="text"
                            value={formValues.surname.value}
                            error={formValues.surname.error}
                            helperText={formValues.surname.error && formValues.surname.errorMessage}
                            onChange={handleChange}
                            fullWidth
                        />
                    </Grid>
                    <Grid item xs={12}>
                        <TextField
                            id="username"
                            name="username"
                            label="Username"
                            type="text"
                            value={formValues.username.value}
                            error={formValues.username.error}
                            helperText={formValues.username.error && formValues.username.errorMessage}
                            disabled
                            fullWidth
                        />
                    </Grid>
                    <Grid item xs={12}>
                        <FormControl sx={{width: "100%"}}>
                            <InputLabel id="select-location-label">Location</InputLabel>
                            <Select
                                id="location"
                                name="location"
                                labelId="select-location-label"
                                value={formValues.location.value}
                                onChange={handleSelectValueChange}
                                fullWidth
                                input={
                                    <OutlinedInput
                                        id="select-location"
                                        label="Location"
                                        multiline={false}
                                    />}
                                MenuProps={MenuProps}
                            >
                                {countries.map((option) => (
                                    <MenuItem key={option.code} value={option.code}>
                                        <img width={20} src={option.flag} alt={option.name}/>&nbsp;{option.name}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Grid>
                    <Grid item xs={12} sx={{
                        margin: "0 auto",
                    }}>
                        <Box sx={{
                            display: "flex",
                            justifyContent: "space-between",
                            width: "100%",
                        }}>
                            <Button
                                variant="outlined"
                                type="button"
                                onClick={onReset}
                                sx={{
                                    margin: 2,
                                    width: "100%",
                                }}
                            >Reset</Button>
                            <Button
                                variant="contained"
                                type="submit"
                                sx={{
                                    margin: 2,
                                    width: "100%",
                                }}
                            >Save</Button>
                        </Box>
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    )
}