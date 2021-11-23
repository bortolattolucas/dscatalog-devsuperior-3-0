import {ReactComponent as ArrowIcon} from 'assets/images/arrow.svg';
import {Link} from 'react-router-dom';
import ProductPrice from "../../components/ProductPrice";
import './styles.css';

const ProductDetails = () => {
    return (
        <div className="product-details-container">
            <div className="base-card product-details-card">
                <Link to="/products">
                    <div className="goback-container">
                        <ArrowIcon/>
                        <h2>VOLTAR</h2>
                    </div>
                </Link>
                <div className="row">
                    <div className="col-xl-6">
                        <div className="img-container">
                            <img
                                src="https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"
                                alt="Produto"/>
                        </div>
                        <div className="name-price-container">
                            <h1>Nome do Produto</h1>
                            <ProductPrice price={2345.64}/>
                        </div>
                    </div>
                    <div className="col-xl-6">
                        <div className="description-container">
                            <h2>Descrição do produto</h2>
                            <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Cupiditate deserunt dolore
                                doloribus eius impedit ipsum natus non nulla, officia repudiandae!</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ProductDetails;