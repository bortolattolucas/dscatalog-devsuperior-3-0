import {Redirect, Route} from 'react-router-dom';
import {isAuthenticated} from 'util/requests';

type Props = {
    children: React.ReactNode;
    path: string;
};

const PrivateRoute = ({children, path}: Props) => {

    return (
        <Route
            path={path}
            render={({location}) =>
                isAuthenticated() ? children : <Redirect to={{
                    pathname: "/admin/auth/login",
                    state: {from: location} //permite seguir pra rota tentada e nao permitida antes
                }}/>
            }
        />
    );
};

export default PrivateRoute;
