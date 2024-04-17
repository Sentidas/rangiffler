import {ApolloClient, InMemoryCache, createHttpLink, Observable, FetchResult, ApolloLink} from "@apollo/client";
import {setContext} from "@apollo/client/link/context";
import {onError} from "@apollo/client/link/error";
import {refreshToken} from "./authUtils.ts";


const BASE_URL = `${import.meta.env.VITE_API_URL}`;

const apolloHttpLink = createHttpLink({
    uri: `${BASE_URL}/graphql`,
})
const errorLink = onError(
    ({ networkError, operation, forward }) => {
        if ((networkError?.statusCode === 401)) {
            return new Observable<FetchResult<Record<string, any>>>(
                (observer) => {
                    (async () => {
                        try {
                            await refreshToken();

                            // Retry the failed request
                            const subscriber = {
                                next: observer.next.bind(observer),
                                error: observer.error.bind(observer),
                                complete: observer.complete.bind(observer),
                            };

                            forward(operation).subscribe(subscriber);
                        } catch (err) {
                            observer.error(err);
                        }
                    })();
                }
            );
        }
    }
);
const headerLink = setContext((_request, previousContext) => ({
    headers: {
        ...previousContext.headers,
        "Authorization": localStorage.getItem("id_token") ? `Bearer ${localStorage.getItem("id_token")}` : "",
    },
}));

export const apiClient = new ApolloClient({
    link: ApolloLink.from([errorLink, headerLink, apolloHttpLink]),
    cache: new InMemoryCache(),
});