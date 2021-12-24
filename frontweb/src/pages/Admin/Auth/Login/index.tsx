import {Link, useHistory, useLocation} from 'react-router-dom';
import ButtonIcon from 'components/ButtonIcon';

import './styles.css';
import {useForm} from "react-hook-form";
import {useContext} from "react";
import {AuthContext} from "../../../../AuthContext";
import {requestBackendLogin} from "../../../../util/requests";
import {saveAuthData} from "../../../../util/storage";
import {getTokenData, isAuthenticated} from "../../../../util/auth";

type CredentialsDTO = {
    username: string;
    password: string;
}

type LocationState = {
    from: string;
}

const Login = () => {

    const location = useLocation<LocationState>();

    const {from} = location.state || {from: {pathname: '/admin'}};

    const {setAuthContextData} = useContext(AuthContext);

    const {register, handleSubmit, formState: {errors}} = useForm<CredentialsDTO>();

    const history = useHistory();

    const onSubmit = (formData: CredentialsDTO) => {
        requestBackendLogin(formData)
            .then(response => {
                saveAuthData(response.data);

                setAuthContextData({
                    authenticated: isAuthenticated(),
                    tokenData: getTokenData()
                });

                history.replace(from); //a rota do login nao vai existir no historico pq vai ser trocada, ai se voltar volta pra pagina que tentou acessar antes de logar
            })
            .catch(error => {
            });
    };

    return (
        <div className="base-card login-card">
            <h1>LOGIN</h1>
            <form onSubmit={handleSubmit(onSubmit)}>
                <div className="mb-4">
                    <input
                        {...register("username", {
                            required: 'Campo obrigatório',
                            pattern: {
                                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                                message: 'E-mail inválido'
                            }
                        })}
                        type="text"
                        className={`form-control base-input ${errors.username ? 'is-invalid' : ''}`}
                        placeholder="Email"
                        name="username"
                    />
                    <div className="invalid-feedback d-block">{errors.username?.message}</div>
                </div>
                <div className="mb-2">
                    <input
                        {...register("password", {
                            required: 'Campo obrigatório'
                        })}
                        type="password"
                        className={`form-control base-input ${errors.password ? 'is-invalid' : ''}`}
                        placeholder="Password"
                        name="password"
                    />
                    <div className="invalid-feedback d-block">{errors.password?.message}</div>
                </div>
                <Link to="/admin/auth/recover" className="login-link-recover">
                    Esqueci a senha
                </Link>
                <div className="login-submit">
                    <ButtonIcon text="Fazer login"/>
                </div>
                <div className="signup-container">
                    <span className="not-registered">Não tem Cadastro?</span>
                    <Link to="/admin/auth/register" className="login-link-register">
                        CADASTRAR
                    </Link>
                </div>
            </form>
        </div>
    );
};

export default Login;
