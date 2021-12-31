import './styles.css';
import {ReactComponent as SearchIcon} from "assets/images/search-icon.svg";
import {Controller, useForm} from "react-hook-form";
import {Category} from "../../types/category";
import Select from "react-select";
import {useEffect, useState} from "react";
import {requestBackend} from "../../util/requests";

export type ProductFilterData = {
    name: string;
    category: Category | null;
}

type Props = {
    onSubmitFilter: (data: ProductFilterData) => void;
}

const ProductFilter = ({onSubmitFilter}: Props) => {

    const [selectCategories, setSelectCategories] = useState<Category[]>([]);

    const {
        register,
        handleSubmit,
        control,
        setValue,
        getValues
    } = useForm<ProductFilterData>();

    const onSubmit = (formData: ProductFilterData) => {
        onSubmitFilter(formData);
    }

    const handleFormClear = () => {
        setValue('name', '');
        setValue('category', null);
    }

    const handleChangeCategory = (value: Category) => {
        setValue('category', value);

        const obj: ProductFilterData = {
            name: getValues('name'),
            category: getValues('category')
        };

        onSubmitFilter(obj);
    }

    useEffect(() => {
        requestBackend({url: '/categories', withCredentials: true})
            .then(response => {
                setSelectCategories(response.data.content)
            })
    }, [])

    return (
        <div className="base-card product-filter-container">
            <form onSubmit={handleSubmit(onSubmit)} className="product-filter-form">
                <div className="product-filter-name-container">
                    <input
                        {...register("name")}
                        type="text"
                        className={"form-control}"}
                        placeholder="Nome do produto"
                        name="name"
                    />
                    <button className="product-filter-button-search">
                        <SearchIcon/>
                    </button>
                </div>
                <div className="product-filter-bottom-container">
                    <div className="product-filter-category-container">
                        <Controller name="category"
                                    control={control}
                                    render={({field}) => (
                                        <Select {...field}
                                                options={selectCategories}
                                                classNamePrefix="product-filter-select"
                                                onChange={value => handleChangeCategory(value as Category)}
                                                getOptionLabel={(cat: Category) => cat.name}
                                                getOptionValue={(cat: Category) => String(cat.id)}
                                                placeholder='Categoria'
                                                isClearable
                                        />
                                    )}
                        />
                    </div>
                    <button onClick={handleFormClear}
                            className="btn btn-outline-secondary btn-product-filter-clear"
                    >
                        LIMPAR<span className="btn-product-filter-word"> FILTROS</span>
                    </button>
                </div>
            </form>
        </div>
    );
}

export default ProductFilter;