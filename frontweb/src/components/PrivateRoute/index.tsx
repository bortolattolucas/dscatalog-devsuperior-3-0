import {Redirect, Route} from 'react-router-dom';
import {hasAnyRoles, isAuthenticated, Role} from 'util/requests';

type Props = {
    children: React.ReactNode;
    path: string;
    roles?: Role[];
};

const PrivateRoute = ({children, path, roles = []}: Props) => {

    return (
        <Route
            path={path}
            render={({location}) =>
                !isAuthenticated() ?
                    //nao autenticado, vai pro login e volta pra onde tentou acessar
                    (<Redirect to={{
                            pathname: "/admin/auth/login",
                            state: {from: location} //permite seguir pra rota tentada e nao permitida antes
                        }}/>
                    ) :
                    !hasAnyRoles(roles) ? (
                        // nao pode acessar a rota
                        <Redirect to='/admin/products'/>
                    ) : (
                        // pode acessar a rota
                        children
                    )
            }
        />
    );
};

export default PrivateRoute;
