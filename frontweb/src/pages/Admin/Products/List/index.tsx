import {Link} from "react-router-dom";
import ProductCrudCard from "../ProductCrudCard";
import './styles.css';
import {useCallback, useEffect, useState} from "react";
import {SpringPage} from "../../../../types/vendor/spring";
import {Product} from "../../../../types/product";
import {AxiosRequestConfig} from "axios";
import {requestBackend} from "../../../../util/requests";
import Pagination from "../../../../components/Pagination";
import ProductFilter, {ProductFilterData} from "../../../../components/ProductFilter";

type ControlComponentsData = {
    activePage: number;
    filterData: ProductFilterData;
}

const List = () => {

    const [page, setPage] = useState<SpringPage<Product>>();

    const [controlComponentsData, setControlComponentsData] = useState<ControlComponentsData>(
        {
            activePage: 0,
            filterData: {name: "", category: null}
        }
    );

    const handlePageChange = (pageNumber: number) => {
        setControlComponentsData({activePage: pageNumber, filterData: controlComponentsData.filterData});
    };

    const handleSubmitFilter = (data: ProductFilterData) => {
        setControlComponentsData({activePage: 0, filterData: data});
    }

    const getProducts = useCallback(() => {
        const config: AxiosRequestConfig = {
            method: 'GET',
            url: '/products',
            params: {
                page: controlComponentsData.activePage,
                size: 3,
                name: controlComponentsData.filterData.name,
                categoryId: controlComponentsData.filterData.category?.id
            }
        };

        requestBackend(config)
            .then(response => {
                    setPage(response.data);
                }
            );
    }, [controlComponentsData]);

    useEffect(() => {
        getProducts();
    }, [getProducts]);


    return (
        <div className="product-crud-container">
            <div className="product-crud-bar-container">
                <Link to="/admin/products/create">
                    <button className="btn btn-primary text-white btn-crud-add">
                        ADICIONAR
                    </button>
                </Link>

                <ProductFilter onSubmitFilter={handleSubmitFilter}/>
            </div>

            <div className="row">
                {
                    page?.content.map(product => (
                        <div key={product.id} className="col-sm-6 col-md-12">
                            <ProductCrudCard product={product} onDelete={getProducts}/>
                        </div>
                    ))
                }
            </div>
            {/* getProducts recebe um numero e é void entao pode passar ali no onchange
                ou seja, o getProducts vai ser chamado pelo onChange(items.selected)
                que seria um getProducts(items.selected) sendo items.selected do componente de paginacao */}
            {/*<Pagination pageCount={(page) ? page.totalPages : 0} range={3} onChange={getProducts}/>*/}
            {/*terceirizado o estado dos componentes pra outro objeto conforme abaixo*/}
            <Pagination forcePage={page?.number} pageCount={(page) ? page.totalPages : 0} range={3}
                        onChange={handlePageChange}/>
        </div>
    );

}

export default List;