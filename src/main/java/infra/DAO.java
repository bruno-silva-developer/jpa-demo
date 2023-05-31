package infra;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

public class DAO<E> {

	private static final String BANCO = "jpa";
	private Class<E> classe;

	private static EntityManagerFactory entityFactory;
	private EntityManager entity;

	static {
		try {
			entityFactory = Persistence.createEntityManagerFactory(BANCO);
		} catch (Exception e) {
			// Log erro -> log4j
		}
	}

	public DAO() {
		this(null);
	}

	public DAO(Class<E> classe) {
		this.classe = classe;
		entity = entityFactory.createEntityManager();
	}

	public DAO<E> abrirT() {
		entity.getTransaction().begin();
		return this;
	}

	public DAO<E> fecharT() {
		entity.getTransaction().commit();
		return this;
	}

	public DAO<E> incluir(E entidade) {
		entity.persist(entidade);
		return this;
	}

	public DAO<E> incluirAtomico(E entidade) {
		return this.abrirT().incluir(entidade).fecharT();
	}

	public E obterPorID(Object id) {
		return entity.find(classe, id);
	}
	public List<E> obterTodos() {
		return this.obterTodos(10, 0);
	}

	public List<E> obterTodos(int qtde, int deslocamento) {
		if (classe == null) {
			throw new UnsupportedOperationException("Classe nula.");
		}

		String jpql = "select e from " + classe.getName() + " e";
		TypedQuery<E> query = entity.createQuery(jpql, classe);
		query.setMaxResults(qtde);
		query.setFirstResult(deslocamento);

		return query.getResultList();
	}
	
	public List<E> consultar(String nomeConsulta, Object... params){
		TypedQuery<E> query = entity.createNamedQuery(nomeConsulta, classe);
		
		for (int i = 0; i < params.length; i += 2) {
			query.setParameter(params[i].toString(), params[i + 1]);
		}
		
		return query.getResultList();
	}
	
	public E consultarUm(String nomeConsulta, Object... params) {
		List<E> lista = consultar(nomeConsulta, params);
		return lista.isEmpty() ? null : lista.get(0);
	}

	public void fechar() {
		entity.close(); 
	}
}
